package net.moewes.cloud.ui.quarkus.deployment;

import io.quarkus.arc.InjectableContext;
import java.lang.annotation.Annotation;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

public class UiContextContext implements InjectableContext {

  @Override
  public void destroy() {

  }

  @Override
  public void destroy(Contextual<?> contextual) {

  }

  @Override
  public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
    return null;
  }

  @Override
  public <T> T get(Contextual<T> contextual) {
    return null;
  }

  @Override
  public Class<? extends Annotation> getScope() {
    return null;
  }

  @Override
  public ContextState getState() {
    return null;
  }

  @Override
  public boolean isActive() {
    return false;
  }
}
