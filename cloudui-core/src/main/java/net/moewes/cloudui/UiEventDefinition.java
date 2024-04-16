package net.moewes.cloudui;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class UiEventDefinition {

    private final String eventName;
    private final List<UiEventAttributeMapping> attributeMappings;
}
