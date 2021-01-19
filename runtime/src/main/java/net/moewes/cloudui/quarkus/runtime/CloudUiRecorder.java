package net.moewes.cloudui.quarkus.runtime;

import java.util.List;
import java.util.logging.Logger;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class CloudUiRecorder {

    private static final Logger log = Logger.getLogger(CloudUiRecorder.class.getName());

    public void registerViews(BeanContainer beanContainer, String view, String path) {
        log.info("register view " + view);
        CloudUiRouter router = beanContainer.instance(CloudUiRouter.class);
        router.addView(view, path);
        // FIXME Refactor One Time is enough
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        router.setClassLoader(contextClassLoader);
    }

    public void touch(BeanContainer beanContainer, List<String> scripts) {
        log.info("register scripts ");
        HtmlPageBuilder pageBuilder = beanContainer.instance(HtmlPageBuilder.class);
        pageBuilder.setScripts(scripts);
    }
}
