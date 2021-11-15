package net.moewes.cloudui.quarkus.runtime;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import net.moewes.cloudui.quarkus.runtime.identity.Identity;
import net.moewes.cloudui.quarkus.runtime.repository.View;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class HtmlPageBuilder {

    @ConfigProperty(name = "quarkus.http.root-path", defaultValue = "")
    String rootPath;

    private List<String> scripts;

    public String getPage(View view, Identity identity) {

        String result = "<!doctype html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"utf-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">";

        String globalScripts = scripts.stream().map(item ->
                "<script src=\"" + getRootPath() + item + "\"></script>").collect(Collectors.joining());

        String viewScripts = view.getScripts().stream().map(item ->
                "<script src=\"" + item.getUrl() + "\" id=\"" + item.getId() + "\" ></script>").collect(Collectors.joining());

        String viewStyles = view.getStyles().stream().map(item ->
                "<link rel=\"stylesheet\" href=\"" + item.getUrl() + "\"></script>").collect(Collectors.joining());

        result = result + globalScripts + viewScripts + getBasicStyle() + viewStyles +
                "</head><body>"
                + getViewContainer(view.getView(), identity)
                + "</body></html>";

        return result;
    }

    private String getRootPath() {

        return "/".equals(rootPath) ? "" : rootPath;
    }

    public void setScripts(List<String> scripts) {
        this.scripts = scripts;
    }

    private String getBasicStyle() {

        return "<style>" +
                "body { margin: 0; padding: 0px } " +
                "</style>";
    }

    private String getViewContainer(String view, Identity identity) {

        String result = "<cloudui-view ";
        result = result + "backend=\"" + getRootPath() + "/" + view + "\" ";
        if (identity.getBearer().isPresent()) {
            result = result + "bearer_token=\"" + identity.getBearer().get() + "\"";
        }
        result = result + "></cloudui-view><body>";
        return result;
    }
}
