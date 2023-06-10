package net.moewes.cloudui.quarkus.runtime;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.moewes.cloudui.UiComponent;
import net.moewes.cloudui.lifecycle.AfterDataBindingObserver;

import java.util.Optional;
import java.util.logging.Logger;

@RequestScoped
public class CloudUi {

    @Inject
    Instance<UiComponent> instance;

    Logger log = Logger.getLogger(this.getClass().getName());

    private Class<? extends UiComponent> nextView;
    private String title;
    private String url;
    private String target;

    private String alertText;

    public void navigate(Class<? extends UiComponent> viewClass) {
        nextView = viewClass;
    }

    Optional<UiComponent> getNextView() {

        if (nextView != null) {
            Instance<? extends UiComponent> view = instance.select(nextView);
            UiComponent uiComponent = view.get();
            uiComponent.setId(nextView.getName());
            if (uiComponent instanceof AfterDataBindingObserver) {
                AfterDataBindingObserver afterDataBindingObserver =
                        (AfterDataBindingObserver) uiComponent;
                afterDataBindingObserver.afterDataBinding();
            }
            return Optional.of(uiComponent);
        }
        return Optional.empty();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void open(String url, String target) {
        this.url = url;
        this.target = target;
    }

    public void changeUrl(String url) {
        this.url = url;
    }

    public void alert(String text) {
        this.alertText = text;
    }

    public ViewResponse getViewResponse() {
        ViewResponse result = new ViewResponse();

        updateViewResponse(result);
        return result;
    }

    public void updateViewResponse(ViewResponse response) {

        if (title != null) {
            response.setTitle(title);
        }
        if (url != null && target != null) {
            response.setNavigation(true);
            response.setUrl(url);
            response.setTarget(target);
        } else if (url != null) {
            response.setChangeUrl(true);
            response.setUrl(url);
        }
        if (alertText != null) {
            response.setAlert(true);
            response.setText(alertText);
        }
    }
}
