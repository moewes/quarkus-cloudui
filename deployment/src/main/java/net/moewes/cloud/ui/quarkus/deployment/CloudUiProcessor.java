package net.moewes.cloud.ui.quarkus.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import java.util.Collection;
import javax.enterprise.context.RequestScoped;
import net.moewes.cloud.ui.annotations.CloudUiView;
import net.moewes.cloud.ui.quarkus.runtime.CloudUiRecorder;
import net.moewes.cloud.ui.quarkus.runtime.CloudUiRouter;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

public class CloudUiProcessor {

  private static final Logger log = Logger.getLogger(CloudUiProcessor.class);
  static private DotName ROUTE_ANNOTATION = DotName.createSimple(CloudUiView.class.getName());
  static private DotName REQUEST_SCOPED = DotName.createSimple(RequestScoped.class.getName());

  @BuildStep
  FeatureBuildItem featureBuildItem() {
    return new FeatureBuildItem("CloudUI");
  }

  @BuildStep
  BeanDefiningAnnotationBuildItem registerX() {
    return new BeanDefiningAnnotationBuildItem(ROUTE_ANNOTATION, REQUEST_SCOPED, false);
  }

  @BuildStep
  AdditionalBeanBuildItem beans() {
    return new AdditionalBeanBuildItem(CloudUiRouter.class);
  }

  @BuildStep
  @Record(STATIC_INIT)
  void scanForBeans(CloudUiRecorder recorder,
      BeanArchiveIndexBuildItem beanArchiveIndex,
      BeanContainerBuildItem beanContainer,
      BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {

    IndexView indexView = beanArchiveIndex.getIndex();
    Collection<AnnotationInstance> cloudUiViews = indexView.getAnnotations(ROUTE_ANNOTATION);
    for (AnnotationInstance annotation : cloudUiViews) {
      log.info("Found " + annotation.target().toString());
      recorder
          .registerViews(beanContainer.getValue(), annotation.target().toString(),
              annotation.value().asString());
    }
  }
}