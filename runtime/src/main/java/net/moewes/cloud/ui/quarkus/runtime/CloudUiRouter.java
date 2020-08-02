package net.moewes.cloud.ui.quarkus.runtime;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import net.moewes.cloud.ui.UiComponent;

@ApplicationScoped
public class CloudUiRouter {

  @Inject
  Instance<Object> instance;

  private Map<String, String> views = new HashMap<>();

  public void addView(String view, String path) {
    System.out.println("View " + view + " added.");
    views.put(path, view);
  }

  public void init(@Observes Router router) {
    System.out.println("Router init");

    views.keySet().stream().forEach(path -> {
      router.get(path).handler(rc -> rc.response().end(HtmlPage.getPage(views.get(path))));
    });

    views.values().stream().forEach(view -> {
      router.get("/" + view).consumes("application/json").handler(BodyHandler.create())
          .handler(this::handleGet);
      router.post("/" + view).consumes("application/json").handler(BodyHandler.create())
          .handler(this::handlePost);
    });

    router.get("/my-route").handler(rc -> rc.response().end("Hello from my route"));

    router.get("/views").handler(rc -> {
      String allViews = views.values().stream().collect(Collectors.joining(", "));
      rc.response().end(allViews);
    });

    router.post("/ui/*").consumes("application/json").handler(BodyHandler.create()).handler(
        this::handlePost);

    router.get("/ui/*").handler((this::handleGet));
  }

  private UiComponent getView() {

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Class<?> viewClass = null;
    try {
      viewClass = Class.forName("net.moewes.ExampleView", true, classLoader);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    UiComponent view = (UiComponent) instance.select(viewClass).get();
    return view;
  }


  private void handleGet(RoutingContext rc) {

    System.out.println(rc.request().method());
    System.out.println(rc.mountPoint());
    System.out.println(rc.request().absoluteURI());
    System.out.println(rc.request().path());

    String result = "[]";

    String substring = rc.normalisedPath().substring(4);
    System.out.println("Path : " + substring);

    ManagedContext requestContext = Arc.container().requestContext();
    if (requestContext.isActive()) {
      result = getString();
    } else {
      try {
        requestContext.activate();
        result = getString();
      } finally {
        requestContext.terminate();
      }
    }
    rc.response().end(result);
  }

  private void handlePost(RoutingContext rc) {

    String result = "[]";

    String substring = rc.normalisedPath().substring(4);
    System.out.println("Path : " + substring);

    ManagedContext requestContext = Arc.container().requestContext();
    if (requestContext.isActive()) {
      result = postString(rc);
    } else {
      try {
        requestContext.activate();
        result = postString(rc);
      } finally {
        requestContext.terminate();
      }
    }
    rc.response().end(result);
  }

  private String getString() { // FIXME Name
    String result;
    UiComponent view = getView();
    view.render();
    result = Json.encode(Arrays.asList(view.getElement()));
    return result;
  }

  private String postString(RoutingContext rc) { // FIXME Name

    Map<String, String> fields = new HashMap<>();
    Map<String, String> events = new HashMap<>();

    UiComponent viewComponent = getView();

    JsonObject json = rc.getBodyAsJson();

    String event = json.getString("event");
    String eventSource = json.getString("id");
    events.put(eventSource, event);

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
          component -> {
            component.handleEvent(events.get(id));
          });
    }
    String result;
    UiComponent view = getView();
    view.render();
    result = Json.encode(Arrays.asList(view.getElement()));
    return result;
  }
}