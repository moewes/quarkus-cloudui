package net.moewes.cloudui.quarkus.runtime.dev;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ViewInfo {

    private String path;
    private String view;
}
