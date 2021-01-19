package net.moewes.cloudui.quarkus.runtime;

import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import io.quarkus.arc.ManagedContext;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.BlockingOperationControl;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import net.moewes.cloudui.UiComponent;

public class AlphaRequestHandler implements Handler<RoutingContext> {

    private static final Logger log = Logger.getLogger("net.moewes.cloudui");

    protected final BeanContainer beanContainer;
    protected final Executor executor;
    protected final ClassLoader classLoader;

    public AlphaRequestHandler(BeanContainer beanContainer, Executor executor, ClassLoader classLoader) {
        this.beanContainer = beanContainer;
        this.executor = executor;
        this.classLoader = classLoader;
    }

    @Override
    public void handle(RoutingContext routingContext) {

        if (BlockingOperationControl.isBlockingAllowed()) {
            try {
                dispatch(routingContext, "net.moewes.cloud.ui.example.cdibinder.ExampleView");
            } catch (Throwable e) {
                routingContext.fail(e);
            }
        } else {
            Vertx vertx = routingContext.vertx();

            vertx.executeBlocking(promise -> {
                String result = dispatch(routingContext, "net.moewes.cloud.ui.example.cdibinder.ExampleView");
                promise.complete(result);
            }, asyncResult -> {
                routingContext.response().end((String) asyncResult.result());
            });
        }
    }

    private String dispatch(RoutingContext rc,
                            String viewClassName) {

        String result = "[]";

        ManagedContext requestContext = beanContainer.requestContext();
        requestContext.activate();

        result = getViewContent(viewClassName);

        requestContext.terminate();
        return result;
    }

    private String getViewContent(String viewClassName) {
        String result;
        UiComponent view = getView(viewClassName);
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
        return (UiComponent) beanContainer.instance(viewClass);
    }
}
