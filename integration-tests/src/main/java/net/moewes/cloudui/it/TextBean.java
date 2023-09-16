package net.moewes.cloudui.it;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class TextBean {

    public String getText() {
        return "text from Bean";
    }
}
