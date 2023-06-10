package net.moewes.cloudui.quarkus.runtime;

import lombok.Data;
import net.moewes.cloudui.UiElement;

@Data
public class ViewResponse {

    private UiElement view;
    private String info = "CloudUi Server Runtime 0.4.0-SNAPShot";
    private String title;
    private String url;
    private String target;
    private String text;
    private boolean navigation = false;
    private boolean alert = false;
    private boolean changeUrl = false;
}
