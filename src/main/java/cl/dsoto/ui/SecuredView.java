package cl.dsoto.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route("secured")
public class SecuredView extends Div implements BeforeEnterObserver {

    public SecuredView() {
        setText("This is a secured view.");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (VaadinSession.getCurrent().getAttribute("username") == null) {
            event.rerouteTo(LoginView.class);
        }
    }
}
