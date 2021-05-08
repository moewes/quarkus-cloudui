package net.moewes.cloudui.quarkus.runtime;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class AlphaRecorder {

    public Handler<RoutingContext> getHandler(BeanContainer beanContainer) {
        return new ViewRequestHandler(beanContainer, Thread.currentThread().getContextClassLoader());
    }
}
