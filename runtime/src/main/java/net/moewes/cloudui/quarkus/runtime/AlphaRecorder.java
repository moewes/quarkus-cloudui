package net.moewes.cloudui.quarkus.runtime;

import java.util.concurrent.Executor;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class AlphaRecorder {

    public Handler<RoutingContext> getHandler(BeanContainer beanContainer, Executor executorProxy) {
        return new AlphaRequestHandler(beanContainer, executorProxy, Thread.currentThread().getContextClassLoader());
    }
}
