package net.moewes.cloud.ui.quarkus.runtime;

public class HtmlPage {

  public static String getPage(String view) {

    String result =
        "<html>" +
            "<head><script src=\"http://localhost:8080/index.js\"></script></head>" +
            "<body><open-dxp-portlet backend=\"http://localhost:8080/" + view
            + "\"></open-dxp-portlet><body>";

    return result;
  }
}
