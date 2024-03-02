package net.moewes.cloudui.quarkus.runtime;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.vertx.http.runtime.HttpConfiguration;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import net.moewes.cloudui.quarkus.runtime.repository.View;

import java.util.List;
import java.util.logging.Logger;

@Recorder
public class CloudUiRecorder {

    private static final Logger log = Logger.getLogger(CloudUiRecorder.class.getName());

    final RuntimeValue<HttpConfiguration> readTimeout;

    public CloudUiRecorder(RuntimeValue<HttpConfiguration> readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void touch(BeanContainer beanContainer, List<String> scripts) {
        log.info("register scripts ");
        HtmlPageBuilder pageBuilder = beanContainer.beanInstance(HtmlPageBuilder.class);
        pageBuilder.setScripts(scripts);
    }

    public Handler<RoutingContext> getPageHandler(BeanContainer beanContainer) {

        return new PageHandler(beanContainer);
    }

    public Handler<RoutingContext> getViewHandler(BeanContainer beanContainer) {
        return new ViewRequestHandler(beanContainer,
                Thread.currentThread().getContextClassLoader(),
                readTimeout.getValue().readTimeout.toMillis());
    }

    public void registerView(BeanContainer beanContainer, View view) {
        CloudUiRouter router = beanContainer.beanInstance(CloudUiRouter.class);
        router.addView(view);
    }
}
