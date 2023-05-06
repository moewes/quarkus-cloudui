package net.moewes.cloudui.quarkus.deployment;

import io.quarkus.arc.deployment.*;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ApplicationArchivesBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.RequestScoped;
import net.moewes.cloudui.annotations.CloudUiView;
import net.moewes.cloudui.annotations.JavaScript;
import net.moewes.cloudui.annotations.StyleSheet;
import net.moewes.cloudui.quarkus.runtime.CloudUi;
import net.moewes.cloudui.quarkus.runtime.CloudUiRecorder;
import net.moewes.cloudui.quarkus.runtime.CloudUiRouter;
import net.moewes.cloudui.quarkus.runtime.HtmlPageBuilder;
import net.moewes.cloudui.quarkus.runtime.identity.IdentityProducer;
import net.moewes.cloudui.quarkus.runtime.repository.Script;
import net.moewes.cloudui.quarkus.runtime.repository.Style;
import net.moewes.cloudui.quarkus.runtime.repository.View;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.webjars.WebJarAssetLocator;

import java.util.*;
import java.util.stream.Collectors;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

public class CloudUiProcessor {

    private static final Logger log = Logger.getLogger(CloudUiProcessor.class);
    static private final DotName VIEW = DotName.createSimple(CloudUiView.class.getName());
    static private final DotName REQUEST_SCOPED =
            DotName.createSimple(RequestScoped.class.getName());
    static private final DotName JAVASCRIPT = DotName.createSimple(JavaScript.class.getName());
    static private final DotName STYLESHEET = DotName.createSimple(StyleSheet.class.getName());

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
        return new AdditionalBeanBuildItem(CloudUiRouter.class,
                CloudUi.class, HtmlPageBuilder.class, IdentityProducer.class);
    }

    @BuildStep
    UnremovableBeanBuildItem unremovableBeans() {
        return UnremovableBeanBuildItem.beanTypes(
                CloudUiRouter.class,
                HtmlPageBuilder.class,
                IdentityProducer.class,
                CloudUi.class);
    }

    @BuildStep
    void scanForEntities(BeanArchiveIndexBuildItem beanArchiveIndex,
                         BuildProducer<ViewBuildItem> buildProducer) {

        IndexView indexView = beanArchiveIndex.getIndex();
        Collection<AnnotationInstance> cloudUiViews = indexView.getAnnotations(VIEW);

        cloudUiViews.forEach(annotationInstance -> {
            String viewname = annotationInstance.target().toString();
            String path = annotationInstance.value().asString();

            buildProducer.produce(new ViewBuildItem(viewname, path));
        });
    }

    @BuildStep
    @Record(STATIC_INIT)
    void scanForViews(CloudUiRecorder recorder,
                      BeanArchiveIndexBuildItem beanArchiveIndex,
                      BeanContainerBuildItem beanContainer,
                      BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
                      BuildProducer<RouteBuildItem> routes) {

        IndexView indexView = beanArchiveIndex.getIndex();
        Collection<AnnotationInstance> cloudUiViews = indexView.getAnnotations(VIEW);

        Handler<RoutingContext> pageHandler = recorder.getPageHandler(beanContainer.getValue());
        Handler<RoutingContext> viewHandler = recorder.getViewHandler(beanContainer.getValue());

        for (AnnotationInstance annotation : cloudUiViews) {
            String viewname = annotation.target().toString();
            String path = annotation.value().asString();

            Set<Script> scripts = new HashSet<>();
            for (AnnotationInstance javascript : annotation.target()
                    .asClass()
                    .declaredAnnotationsWithRepeatable(JAVASCRIPT, indexView)) {
                Script script = new Script();
                script.setUrl(javascript.value().asString());
                script.setId(javascript.value("id").asString());
                scripts.add(script);
            }

            Set<Style> styles = new HashSet<>();
            for (AnnotationInstance stylesheet : annotation.target()
                    .asClass()
                    .declaredAnnotationsWithRepeatable(STYLESHEET, indexView)) {
                Style style = new Style();
                style.setUrl(stylesheet.value().asString());
                styles.add(style);
            }

            View view = new View();
            view.setView(viewname);
            view.setPath(path);
            view.setScripts(scripts);
            view.setStyles(styles);

            recorder.registerView(beanContainer.getValue(), view);

            routes.produce(RouteBuildItem.builder().route(path).handler(pageHandler).build());
            routes.produce(RouteBuildItem.builder()
                    .route("/" + viewname)
                    .handler(viewHandler)
                    .build());
        }
    }

    @BuildStep
    @Record(STATIC_INIT)
    void scanWebjarResources(CloudUiRecorder recorder, BeanContainerBuildItem beanContainer,
                             ApplicationArchivesBuildItem applicationArchivesBuildItem) {

        log.info("extract resources from webjar");
        WebJarAssetLocator webJarLocator = new WebJarAssetLocator();
        Map<String, String> webjarNameToVersionMap = webJarLocator.getWebJars();
        webjarNameToVersionMap.keySet()
                .forEach(item -> log.info(item + " " + webjarNameToVersionMap.get(item)));

        List<String> scripts =
                webJarLocator.listAssets().stream().filter(item -> item.endsWith(".js"))
                        .map(item -> item.replace("META-INF/resources", ""))
                        .collect(Collectors.toList());

        recorder.touch(beanContainer.getValue(), scripts);
    }

    @BuildStep
    ReflectiveClassBuildItem reflection() {
        return ReflectiveClassBuildItem.builder("net.moewes.cloudui.UiElement", "net.moewes" +
                ".cloudui.UiElementAttribute").methods(true).fields(true).build();
    }
}