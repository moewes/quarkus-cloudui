package net.moewes.cloud.ui.quarkus.runtime;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;
import java.util.List;
import java.util.logging.Logger;

@Recorder
public class CloudUiRecorder {

  private static final Logger log = Logger.getLogger(CloudUiRecorder.class.getName());

  public void registerViews(BeanContainer beanContainer, String view, String path) {
    log.info("register view " + view);
    CloudUiRouter router = beanContainer.instance(CloudUiRouter.class);
    router.addView(view, path);
  }

  public void touch(BeanContainer beanContainer, List<String> scripts) {
    log.info("register scripts ");
    HtmlPageBuilder pageBuilder = beanContainer.instance(HtmlPageBuilder.class);
    pageBuilder.setScripts(scripts);
  }
}
