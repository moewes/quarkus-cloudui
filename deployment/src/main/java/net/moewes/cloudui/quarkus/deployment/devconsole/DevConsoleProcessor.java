package net.moewes.cloudui.quarkus.deployment.devconsole;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import net.moewes.cloudui.quarkus.deployment.ViewBuildItem;
import net.moewes.cloudui.quarkus.runtime.dev.ViewInfoRpcService;

import java.util.List;

public class DevConsoleProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    CardPageBuildItem create(List<ViewBuildItem> viewBuildItems) {
        CardPageBuildItem cardPageBuildItem = new CardPageBuildItem();

        cardPageBuildItem.addPage(Page.webComponentPageBuilder()
                .icon("font-awesome-solid:route")
                .title("Views")
                .componentLink("dev-ui-views.js")
                .staticLabel(String.valueOf(viewBuildItems.size()))
        );
        return cardPageBuildItem;
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    JsonRPCProvidersBuildItem createJsonRPCService() {
        return new JsonRPCProvidersBuildItem("CloudUi", ViewInfoRpcService.class);
    }
}
