package net.moewes.cloud.ui.quarkus.runtime;

import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HtmlPageBuilder {

  private List<String> scripts;

  public String getPage(String view) {

    String result =
        "<html>" +
            "<head>";

    String script = scripts.stream().map(item ->
        "<script src=\"" + item + "\"></script>").collect(Collectors.joining());

    result = result + script +
        "</head>" +
        "<body><open-dxp-portlet backend=\"/" + view
        + "\"></open-dxp-portlet><body>";

    return result;
  }

  public void setScripts(List<String> scripts) {
    this.scripts = scripts;
  }
}
