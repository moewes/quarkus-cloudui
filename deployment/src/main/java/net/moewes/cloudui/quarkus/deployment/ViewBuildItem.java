package net.moewes.cloudui.quarkus.deployment;

import io.quarkus.builder.item.MultiBuildItem;

public final class ViewBuildItem extends MultiBuildItem {

    final String name;
    final String path;

    public ViewBuildItem(String name, String path) {
        this.name = name;
        this.path = path;
    }
}
