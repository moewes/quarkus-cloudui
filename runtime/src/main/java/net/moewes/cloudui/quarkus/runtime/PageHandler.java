package net.moewes.cloudui.quarkus.runtime;

import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class PageHandler implements Handler<RoutingContext> {
    private static final Logger log = Logger.getLogger(PageHandler.class.getName());

    private HtmlPageBuilder pageBuilder;
    private CloudUiRouter cloudUiRouter;

    public PageHandler() {
        log.info("constructor");
        cloudUiRouter = CDI.current().select(CloudUiRouter.class).get();
        pageBuilder = CDI.current().select(HtmlPageBuilder.class).get();
    }

    @Override
    public void handle(RoutingContext routingContext) {

        String path = routingContext.request().path();
        log.info("handle " + path);
        routingContext.response().end(pageBuilder.getPage(cloudUiRouter.getViewFromPath(path)));
    }
}
