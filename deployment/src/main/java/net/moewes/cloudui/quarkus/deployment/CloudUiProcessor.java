package net.moewes.cloudui.quarkus.deployment;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ApplicationArchivesBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import net.moewes.cloudui.annotations.CloudUiView;
import net.moewes.cloudui.quarkus.runtime.CloudUi;
import net.moewes.cloudui.quarkus.runtime.CloudUiRecorder;
import net.moewes.cloudui.quarkus.runtime.CloudUiRouter;
import net.moewes.cloudui.quarkus.runtime.HtmlPageBuilder;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.webjars.WebJarAssetLocator;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

public class CloudUiProcessor {

    private static final Logger log = Logger.getLogger(CloudUiProcessor.class);
    static private final DotName VIEW = DotName.createSimple(CloudUiView.class.getName());
    static private final DotName REQUEST_SCOPED = DotName.createSimple(RequestScoped.class.getName());

    @BuildStep
    FeatureBuildItem featureBuildItem() {
        return new FeatureBuildItem("CloudUI");
    }

    @BuildStep
    BeanDefiningAnnotationBuildItem registerViewAnnotation() {
        return new BeanDefiningAnnotationBuildItem(VIEW, REQUEST_SCOPED, true);
    }

    @BuildStep
    AdditionalBeanBuildItem beans() {
        return new AdditionalBeanBuildItem(CloudUiRouter.class, CloudUi.class, HtmlPageBuilder.class);
    }

    @BuildStep
    @Record(STATIC_INIT)
    void scanForViews(CloudUiRecorder recorder,
                      BeanArchiveIndexBuildItem beanArchiveIndex,
                      BeanContainerBuildItem beanContainer,
                      BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {

        IndexView indexView = beanArchiveIndex.getIndex();
        Collection<AnnotationInstance> cloudUiViews = indexView.getAnnotations(VIEW);
        for (AnnotationInstance annotation : cloudUiViews) {
            log.info("Found " + annotation.target().toString());
            recorder
                    .registerViews(beanContainer.getValue(), annotation.target().toString(),
                            annotation.value().asString());
        }
    }

    @BuildStep
    @Record(STATIC_INIT)
    void scanWebjarResources(CloudUiRecorder recorder, BeanContainerBuildItem beanContainer,
                             ApplicationArchivesBuildItem applicationArchivesBuildItem) {

        log.info("extract resources from webjar");
        WebJarAssetLocator webJarLocator = new WebJarAssetLocator();
        Map<String, String> webjarNameToVersionMap = webJarLocator.getWebJars();
        webjarNameToVersionMap.keySet().stream()
                .forEach(item -> log.info(item + " " + webjarNameToVersionMap.get(item)));

        List<String> scripts = webJarLocator.listAssets().stream().filter(item -> item.endsWith(".js"))
                .map(item -> item.replace("META-INF/resources", ""))
                .collect(Collectors.toList());

        recorder.touch(beanContainer.getValue(), scripts);
    }

    @BuildStep
    ReflectiveClassBuildItem reflection() {
        return new ReflectiveClassBuildItem(true, true,
                "net.moewes.cloudui.UiElement", "net.moewes.cloudui.UiElementAttribute");
    }
}