package net.moewes.cloudui.quarkus.runtime;

import java.util.List;
import java.util.logging.Logger;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class CloudUiRecorder {

    private static final Logger log = Logger.getLogger(CloudUiRecorder.class.getName());

    public void registerView(BeanContainer beanContainer, String view, String path) {
        CloudUiRouter router = beanContainer.instance(CloudUiRouter.class);
        router.addView(view, path);
    }

    public void touch(BeanContainer beanContainer, List<String> scripts) {
        log.info("register scripts ");
        HtmlPageBuilder pageBuilder = beanContainer.instance(HtmlPageBuilder.class);
        pageBuilder.setScripts(scripts);
    }

    public Handler<RoutingContext> getPageHandler() {

        return new PageHandler();
    }

    public Handler<RoutingContext> getViewHandler(BeanContainer beanContainer) {
        return new ViewRequestHandler(beanContainer, Thread.currentThread().getContextClassLoader());
    }
}
