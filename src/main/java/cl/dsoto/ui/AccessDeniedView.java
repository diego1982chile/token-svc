package cl.dsoto.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;

@Route("access-denied")
@PageTitle("Access Denied")
public class AccessDeniedView extends Div {

    public AccessDeniedView() {
        setText("Access Denied. You do not have permission to view this page.");
    }
}