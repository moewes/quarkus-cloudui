package net.moewes.cloudui.quarkus.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.CDI;

import io.quarkus.arc.ManagedContext;
import io.quarkus.arc.runtime.BeanContainer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.moewes.cloudui.UiComponent;
import net.moewes.cloudui.UiEvent;

public class ViewRequestHandler implements Handler<RoutingContext> {

    private static final Logger log = Logger.getLogger("net.moewes.cloudui");

    protected final BeanContainer beanContainer;
    protected final ClassLoader classLoader;
    private CloudUiRouter cloudUiRouter;

    public ViewRequestHandler(BeanContainer beanContainer, ClassLoader classLoader) {
        this.beanContainer = beanContainer;
        this.classLoader = classLoader;
        cloudUiRouter = CDI.current().select(CloudUiRouter.class).get();
    }

    @Override
    public void handle(RoutingContext routingContext) {

        int i = 1;
        if (!"/".equals(cloudUiRouter.getRootPath())) {
            i = cloudUiRouter.getRootPath().length() + 1;
        }

        String view = routingContext.request().path().substring(i);
        log.info("view " + view);

        if (routingContext.request().method() == HttpMethod.GET) {
            Vertx vertx = routingContext.vertx();

            vertx.executeBlocking(promise -> {
                String result = dispatch(routingContext, null, view);
                promise.complete(result);
            }, asyncResult -> {
                routingContext.response().end((String) asyncResult.result());
            });
        } else if (routingContext.request().method() == HttpMethod.POST) {
            routingContext.request().bodyHandler(buffer -> {
                final JsonObject body = buffer.toJsonObject();

                Vertx vertx = routingContext.vertx();

                vertx.executeBlocking(promise -> {
                    String result = dispatch(routingContext, body, view);
                    promise.complete(result);
                }, asyncResult -> {
                    routingContext.response().end((String) asyncResult.result());
                });
            });
        } else {
            routingContext.fail(405);
        }
    }

    private String dispatch(RoutingContext rc, JsonObject json,
                            String viewClassName) {

        String result = "[]";

        ManagedContext requestContext = beanContainer.requestContext();
        requestContext.activate();

        try {
            if (rc.request().method() == HttpMethod.GET) {
                result = getViewContent(viewClassName);
            } else if (rc.request().method() == HttpMethod.POST) {
                result = processViewEvent(json, viewClassName);
            } else {
                rc.fail(405);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        requestContext.terminate();
        return result;
    }

    private String getViewContent(String viewClassName) {
        String result;
        UiComponent view = getView(viewClassName);
        view.setId(viewClassName);
        view.render();
        result = Json.encode(Collections.singletonList(view.getElement()));
        return result;
    }

    private String processViewEvent(JsonObject json, String viewClassName) {

        Map<String, String> fields = new HashMap<>();
        Map<String, UiEvent> events = new HashMap<>();

        UiComponent viewComponent = getView(viewClassName);
        viewComponent.setId(viewClassName);

        String eventname = json.getString("event");
        String eventSource = json.getString("id");
        Map<String, Object> eventMap;
        try {
            eventMap = json.getJsonObject("details").getMap();
        } catch (java.lang.ClassCastException e) {
            eventMap = new HashMap<>();
        }
        events.put(eventSource, new UiEvent(eventname, eventMap));

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

        for (String id : events.keySet()) {
            viewComponent.getComponentWithId(id).ifPresent(
                    component -> component.handleEvent(events.get(id)));
        }
        String result;
        CloudUi ui = CDI.current().select(CloudUi.class).get();
        UiComponent view = ui.getNextView().orElse(viewComponent);
        view.render();
        result = Json.encode(Collections.singletonList(view.getElement()));
        return result;
    }

    private UiComponent getView(String viewClassName) {

        Class<?> viewClass = null;
        try {
            viewClass = Class.forName(viewClassName, true, classLoader);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Object instance = beanContainer.instance(viewClass);
        return (UiComponent) CDI.current().select(viewClass, Default.Literal.INSTANCE).get();
    }
}
