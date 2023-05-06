package net.moewes.cloudui.quarkus.deployment.devconsole;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.pkg.builditem.CurateOutcomeBuildItem;
import io.quarkus.devconsole.spi.DevConsoleRuntimeTemplateInfoBuildItem;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import net.moewes.cloudui.quarkus.deployment.ViewBuildItem;
import net.moewes.cloudui.quarkus.runtime.dev.ViewInfoRpcService;
import net.moewes.cloudui.quarkus.runtime.dev.ViewInfoSupplier;

import java.util.List;

public class DevConsoleProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    public DevConsoleRuntimeTemplateInfoBuildItem collectBeanInfo(CurateOutcomeBuildItem curateOutcomeBuildItem) {
        return new DevConsoleRuntimeTemplateInfoBuildItem("views",
                new ViewInfoSupplier(), this.getClass(), curateOutcomeBuildItem);
    }

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
