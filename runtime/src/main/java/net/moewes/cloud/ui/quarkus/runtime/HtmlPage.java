package net.moewes.cloud.ui.quarkus.runtime;

public class HtmlPage {

  public static String getPage(String view) {

    String result =
        "<html>" +
            "<head><script src=\"/webjars/cloud-ui/index.js\"></script></head>" +
            "<body><open-dxp-portlet backend=\"/" + view
            + "\"></open-dxp-portlet><body>";

    return result;
  }
}
