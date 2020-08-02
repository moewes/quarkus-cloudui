package org.acme;

import io.vertx.ext.web.Router;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class Portal {

  @Inject
  PortalBean bean;

  public void init(@Observes Router router) {
    router.get("/my-route").handler(rc -> rc.response().end("Hello from my route"));

    router.get("/").handler(rc -> rc.reroute("/dxp/"));
    router.get("/dxp*").handler(rc -> rc.response().end(bean.getPortalPage()));

    router.get("/context").handler(rc -> {
      String absoluteURI = rc.request().absoluteURI();
      String path = rc.request().localAddress().path();

      rc.response().end(" absuluteUri " + absoluteURI + ", local " + path);
    });
  }

}