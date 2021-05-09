package net.moewes.cloudui.quarkus.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import net.moewes.cloudui.quarkus.runtime.dev.ViewInfo;

@ApplicationScoped
public class CloudUiRouter {

    private final Map<String, String> views = new HashMap<>();
    @Inject
    Instance<Object> instance;

    @Inject
    HtmlPageBuilder pageBuilder;

    public void addView(String view, String path) {
        views.put(path, view);
    }

    public String getViewFromPath(String path) {
        return views.get(path);
    }

    public List<ViewInfo> getAllViews() {
        ArrayList<ViewInfo> result = new ArrayList<>();

        views.forEach((path, view) -> {
            result.add(ViewInfo.builder().view(view).path(path).build());
        });
        return result;
    }
}