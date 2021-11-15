package net.moewes.cloudui.quarkus.runtime.repository;

import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class View {

    private String path;
    private String view;
    private Set<Script> scripts;
    private Set<Style> styles;
}
