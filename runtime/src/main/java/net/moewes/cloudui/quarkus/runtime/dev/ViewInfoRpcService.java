package net.moewes.cloudui.quarkus.runtime.dev;

import io.quarkus.arc.Arc;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import net.moewes.cloudui.quarkus.runtime.CloudUiRouter;

import java.util.Collection;

@ApplicationScoped
public class ViewInfoRpcService {

    public JsonArray getInfo() {
        JsonArray result = new JsonArray();

        Arc.container().instance(CloudUiRouter.class).get().getAllViews().forEach(item -> {
            JsonObject object = new JsonObject();
            object.put("view",item.getView());
            object.put("path",item.getPath());
            result.add(object);
                }
        );
        return result;
    }

    public String getLabel() {
        return "123";
    }
}
