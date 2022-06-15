package net.moewes.cloudui.quarkus.runtime.dev;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ViewInfo {

    String path;
    String view;
}
