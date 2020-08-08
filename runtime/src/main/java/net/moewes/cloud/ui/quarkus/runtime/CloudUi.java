package net.moewes.cloud.ui.quarkus.runtime;

import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import net.moewes.cloud.ui.UiComponent;

@RequestScoped
public class CloudUi {

  @Inject
  Instance<UiComponent> instance;

  Logger log = Logger.getLogger(this.getClass().getName());

  private Class<? extends UiComponent> nextView;

  @PostConstruct
  public void init() {
    log.info("init");
  }

  public void navigate(Class<? extends UiComponent> viewClass) {
    nextView = viewClass;
  }

  Optional<UiComponent> getNextView() {

    if (nextView != null) {
      Instance<? extends UiComponent> view = instance.select(nextView);
      return Optional.ofNullable(view.get());
    }
    return Optional.empty();
  }


}
