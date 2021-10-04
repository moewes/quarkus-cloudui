package net.moewes.cloudui.quarkus.runtime;

import java.util.logging.Logger;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import io.quarkus.arc.ManagedContext;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import net.moewes.cloudui.quarkus.runtime.identity.Identity;

public class PageHandler implements Handler<RoutingContext> {
    private static final Logger log = Logger.getLogger(PageHandler.class.getName());
    protected final BeanContainer beanContainer;
    private CurrentIdentityAssociation association;
    private HtmlPageBuilder pageBuilder;
    private CloudUiRouter cloudUiRouter;

    public PageHandler(BeanContainer beanContainer) {
        this.beanContainer = beanContainer;
        cloudUiRouter = CDI.current().select(CloudUiRouter.class).get();
        pageBuilder = CDI.current().select(HtmlPageBuilder.class).get();

        Instance<CurrentIdentityAssociation> association = CDI.current().select(CurrentIdentityAssociation.class);
        this.association = association.isResolvable() ? association.get() : null;
    }

    @Override
    public void handle(RoutingContext routingContext) {

        Vertx vertx = routingContext.vertx();

        vertx.executeBlocking(promise -> {
            String result = dispatch(routingContext);
            promise.complete(result);
        }, asyncResult -> {
            routingContext.response().end((String) asyncResult.result());
        });
    }

    private String dispatch(RoutingContext routingContext) {

        String path = routingContext.request().path();
        log.info("handle " + path);

        ManagedContext requestContext = beanContainer.requestContext();
        requestContext.activate();

        if (association != null) {
            QuarkusHttpUser existing = (QuarkusHttpUser) routingContext.user();
            if (existing != null) {
                SecurityIdentity identity = existing.getSecurityIdentity();
                association.setIdentity(identity);
            } else {
                association.setIdentity(QuarkusHttpUser.getSecurityIdentity(routingContext, null));
            }
        }

        Identity identity = CDI.current().select(Identity.class).get();
        String result = pageBuilder.getPage(cloudUiRouter.getViewFromPath(path), identity);
        requestContext.terminate();
        return result;
    }
}
