package net.moewes.cloudui.quarkus.runtime;

import io.quarkus.arc.ManagedContext;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.VertxInputStream;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import net.moewes.cloudui.UiComponent;
import net.moewes.cloudui.UiEvent;
import net.moewes.cloudui.lifecycle.AfterDataBindingObserver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ViewRequestHandler implements Handler<RoutingContext> {

    private static final Logger log = Logger.getLogger("net.moewes.cloudui");

    protected final BeanContainer beanContainer;
    protected final ClassLoader classLoader;
    private final CloudUiRouter cloudUiRouter;
    private final CurrentIdentityAssociation association;

    private final long readTimeout;

    public ViewRequestHandler(BeanContainer beanContainer, ClassLoader classLoader,
                              long readTimeout) {
        this.beanContainer = beanContainer;
        this.classLoader = classLoader;
        this.readTimeout = readTimeout;
        cloudUiRouter = CDI.current().select(CloudUiRouter.class).get();
        Instance<CurrentIdentityAssociation> association =
                CDI.current().select(CurrentIdentityAssociation.class);
        this.association = association.isResolvable() ? association.get() : null;
    }

    @Override
    public void handle(RoutingContext routingContext) {

        String viewName = getViewName(routingContext);

        Vertx vertx = routingContext.vertx();
        if (routingContext.request().method() == HttpMethod.GET) {
            vertx.executeBlocking(promise -> {
                String result = dispatch(routingContext, null, viewName);
                promise.complete(result);
            }, asyncResult -> routingContext.response().end((String) asyncResult.result()));
        } else if (routingContext.request().method() == HttpMethod.POST) {
            InputStream is;
            if (routingContext.getBody() != null) {
                is = new ByteArrayInputStream(routingContext.getBody().getBytes());
            } else {
                is = new VertxInputStream(routingContext, readTimeout);
            }
            vertx.executeBlocking(promise -> {

                Buffer buffer;
                try {
                    buffer = Buffer.buffer(is.readAllBytes());
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                final JsonObject body = buffer.toJsonObject();

                String result = dispatch(routingContext, body, viewName);
                promise.complete(result);

            }, asyncResult -> routingContext.response().end((String) asyncResult.result()));


        } else {
            routingContext.fail(405);
        }
    }

    private String getViewName(RoutingContext routingContext) {
        int i = 1;
        if (!"/".equals(cloudUiRouter.getRootPath())) {
            i = cloudUiRouter.getRootPath().length() + 1;
        }

        return routingContext.request().path().substring(i);
    }

    private String dispatch(RoutingContext rc, JsonObject json,
                            String viewClassName) {

        String result = "[]";

        ManagedContext requestContext = beanContainer.requestContext();
        requestContext.activate();

        if (association != null) {
            QuarkusHttpUser existing = (QuarkusHttpUser) rc.user();
            if (existing != null) {
                SecurityIdentity identity = existing.getSecurityIdentity();
                association.setIdentity(identity);
            } else {
                association.setIdentity(QuarkusHttpUser.getSecurityIdentity(rc, null));
            }
        }

        try {
            if (rc.request().method() == HttpMethod.GET) {
                result = getViewContent(viewClassName);
            } else if (rc.request().method() == HttpMethod.POST) {
                result = processViewEvent(json, viewClassName);
            } else {
                rc.fail(405);
            }
        } catch (Exception e) {
            log.info(e.getLocalizedMessage());
            rc.fail(500);
        }

        requestContext.terminate();
        return result;
    }

    private String getViewContent(String viewClassName) {
        String result;
        CloudUi ui = CDI.current().select(CloudUi.class).get();
        UiComponent view = getView(viewClassName);
        view.setId(viewClassName);

        if (view instanceof AfterDataBindingObserver afterDataBindingObserver) {
            afterDataBindingObserver.afterDataBinding();
        }

        view.render();
        ViewResponse viewResponse = ui.getViewResponse();
        viewResponse.setView(view.getElement());
        result = Json.encode(viewResponse);
        return result;
    }

    private String processViewEvent(JsonObject json, String viewClassName) {

        Map<String, String> fields = new HashMap<>();
        Map<String, UiEvent> events = new HashMap<>();

        UiComponent viewComponent = getView(viewClassName);
        viewComponent.setId(viewClassName);

        String eventName = json.getString("event");
        String eventSource = json.getString("id");
        Map<String, Object> eventMap;
        try {
            eventMap = json.getJsonObject("details").getMap();
        } catch (java.lang.ClassCastException e) {
            eventMap = new HashMap<>();
        }
        events.put(eventSource, new UiEvent(eventName, eventMap));

        json.getJsonArray("fields").forEach(item -> {
            JsonObject fieldObject = (JsonObject) item;
            String fieldName = fieldObject.getString("name");
            String fieldValue = fieldObject.getString("value");
            fields.put(fieldName, fieldValue);

        });

        for (String id : fields.keySet()) {
            viewComponent.getComponentWithId(id).ifPresent(
                    component -> {
                        component.setValue(fields.get(id));
                        component.setValuesWithBinder();
                    });
        }

        if (viewComponent instanceof AfterDataBindingObserver afterDataBindingObserver) {
            afterDataBindingObserver.afterDataBinding();
        }

        for (String id : events.keySet()) {
            viewComponent.getComponentWithId(id).ifPresent(
                    component -> component.handleEvent(events.get(id)));
        }
        String result;
        CloudUi ui = CDI.current().select(CloudUi.class).get();
        UiComponent view = ui.getNextView().orElse(viewComponent);
        ViewResponse viewResponse = ui.getViewResponse();
        view.render();
        viewResponse.setView(view.getElement());
        result = Json.encode(viewResponse);
        return result;
    }

    private UiComponent getView(String viewClassName) {

        Class<?> viewClass = null;
        try {
            viewClass = Class.forName(viewClassName, true, classLoader);
        } catch (ClassNotFoundException e) {
            log.info("view class " + viewClassName + " cannot be found");
        }
        return (UiComponent) CDI.current().select(viewClass, Default.Literal.INSTANCE).get();
    }
}
