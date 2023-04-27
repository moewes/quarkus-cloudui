package net.moewes.cloudui.quarkus.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import jakarta.enterprise.context.ApplicationScoped;
import net.moewes.cloudui.quarkus.runtime.dev.ViewInfo;
import net.moewes.cloudui.quarkus.runtime.repository.View;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CloudUiRouter {

    @ConfigProperty(name = "quarkus.http.root-path", defaultValue = "")
    String rootPath;

    private final Map<String, View> views = new HashMap<>();

    public View getViewFromPath(String path) {
        return views.get(path);
    }

    // TODO Refactor -> List<View> hier nicht ViewInfo nutzen
    public List<ViewInfo> getAllViews() {
        ArrayList<ViewInfo> result = new ArrayList<>();

        views.forEach((path, view) -> result.add(ViewInfo.builder().view(view.getView()).path(path).build()));
        return result;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void addView(View view) {
        if ("/".equals(rootPath)) {
            views.put(view.getPath(), view);
        } else {
            views.put(rootPath + view.getPath(), view);
        }
    }
}