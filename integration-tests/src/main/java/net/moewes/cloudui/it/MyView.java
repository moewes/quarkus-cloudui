package net.moewes.cloudui.it;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import net.moewes.cloudui.annotations.CloudUiView;
import net.moewes.cloudui.html.Div;
import net.moewes.cloudui.html.H1;

@CloudUiView("/")
public class MyView extends Div {

    @Inject
    TextBean bean;

    @PostConstruct
    public void init() {
        add(new H1(bean.getText()));
    }
}
