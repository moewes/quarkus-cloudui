package net.moewes.cloudui.quarkus.deployment;

import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ExecutorBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import net.moewes.cloudui.quarkus.runtime.AlphaRecorder;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

public class Alpha {

    @BuildStep
    @Record(RUNTIME_INIT)
    public void build(BuildProducer<RouteBuildItem> routes, BeanContainerBuildItem beanContainer, AlphaRecorder recorder, ExecutorBuildItem executorBuildItem) {

        Handler<RoutingContext> handler = recorder.getHandler(beanContainer.getValue(), executorBuildItem.getExecutorProxy());

        routes.produce(new RouteBuildItem("/alpha", handler));
    }
}
