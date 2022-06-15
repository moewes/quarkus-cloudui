package net.moewes.cloudui.quarkus.runtime;

import java.util.Optional;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import net.moewes.cloudui.UiComponent;
import net.moewes.cloudui.lifecycle.AfterDataBindingObserver;

@RequestScoped
public class CloudUi {

    @Inject
    Instance<UiComponent> instance;

    Logger log = Logger.getLogger(this.getClass().getName());

    private Class<? extends UiComponent> nextView;

    public void navigate(Class<? extends UiComponent> viewClass) {
        nextView = viewClass;
    }

    Optional<UiComponent> getNextView() {

        if (nextView != null) {
            Instance<? extends UiComponent> view = instance.select(nextView);
            UiComponent uiComponent = view.get();
            uiComponent.setId(nextView.getName());
            if (uiComponent instanceof AfterDataBindingObserver) {
                AfterDataBindingObserver afterDataBindingObserver = (AfterDataBindingObserver) uiComponent;
                afterDataBindingObserver.afterDataBinding();
            }
            return Optional.of(uiComponent);
        }
        return Optional.empty();
    }


}
