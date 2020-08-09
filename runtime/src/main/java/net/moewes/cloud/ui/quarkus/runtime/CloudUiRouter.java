package net.moewes.cloud.ui.quarkus.runtime;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import net.moewes.cloud.ui.UiComponent;

@ApplicationScoped
public class CloudUiRouter {

  private final Map<String, String> views = new HashMap<>();
  @Inject
  Instance<Object> instance;

  @Inject
  HtmlPageBuilder pageBuilder;

  public void addView(String view, String path) {
    System.out.println("View " + view + " added.");
    views.put(path, view);
  }

  public void init(@Observes Router router) {
    System.out.println("Router init");

    views.keySet().stream().forEach(path -> {
      router.get(path).handler(rc -> rc.response().end(pageBuilder.getPage(views.get(path))));
    });

    views.values().stream().forEach(view -> {
      router.get("/" + view).consumes("application/json").handler(BodyHandler.create())
          .handler(rc -> handleGet(rc, view));
      router.post("/" + view).consumes("application/json").handler(BodyHandler.create())
          .handler(rc -> handlePost(rc, view));
    });

    router.get("/views").handler(rc -> {
      String allViews = String.join(", ", views.values());
      rc.response().end(allViews);
    });
  }

  private UiComponent getView(String viewClassName) {

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Class<?> viewClass = null;
    try {
      viewClass = Class.forName(viewClassName, true, classLoader);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return (UiComponent) instance.select(viewClass).get();
  }


  private void handleGet(RoutingContext rc, String viewClassName) {

    String result = "[]";

    ManagedContext requestContext = Arc.container().requestContext();
    if (requestContext.isActive()) {
      result = getViewContent(viewClassName);
    } else {
      try {
        requestContext.activate();
        result = getViewContent(viewClassName);
      } finally {
        requestContext.terminate();
      }
    }
    rc.response().end(result);
  }

  private void handlePost(RoutingContext rc, String viewClassName) {

    String result = "[]";

    ManagedContext requestContext = Arc.container().requestContext();
    if (requestContext.isActive()) {
      result = processViewEvent(rc, viewClassName);
    } else {
      try {
        requestContext.activate();
        result = processViewEvent(rc, viewClassName);
      } finally {
        requestContext.terminate();
      }
    }
    rc.response().end(result);
  }

  private String getViewContent(String viewClassName) {
    String result;
    UiComponent view = getView(viewClassName);
    view.render();
    result = Json.encode(Collections.singletonList(view.getElement()));
    return result;
  }

  private String processViewEvent(RoutingContext rc, String viewClassName) {

    Map<String, String> fields = new HashMap<>();
    Map<String, String> events = new HashMap<>();

    CloudUi ui = instance.select(CloudUi.class).get();
    UiComponent viewComponent = getView(viewClassName);

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
          component -> component.handleEvent(events.get(id)));
    }
    String result;
    UiComponent view = ui.getNextView().orElse(viewComponent);
    view.render();
    result = Json.encode(Collections.singletonList(view.getElement()));
    return result;
  }
}