package net.moewes.cloudui.quarkus.runtime.dev;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import io.quarkus.arc.Arc;
import net.moewes.cloudui.quarkus.runtime.CloudUiRouter;

public class ViewInfoSupplier implements Supplier<Collection<ViewInfo>> {

    @Override
    public Collection<ViewInfo> get() {
        List<ViewInfo> views = new ArrayList<>(allViews());
        // TODO sort
        return views;
    }

    public static List<ViewInfo> allViews() {
        return Arc.container().instance(CloudUiRouter.class).get().getAllViews();
    }
}
