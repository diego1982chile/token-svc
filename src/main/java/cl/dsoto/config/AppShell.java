package cl.dsoto.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;

@CssImport("./styles/alta-shell.css")
@JsModule("./theme-init.js")
public class AppShell implements AppShellConfigurator {
}
