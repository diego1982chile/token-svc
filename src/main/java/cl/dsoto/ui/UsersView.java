package cl.dsoto.ui;

import cl.dsoto.entities.Role;
import cl.dsoto.entities.User;
import cl.dsoto.services.RoleService;
import cl.dsoto.services.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

import java.util.List;

import static java.util.Collections.EMPTY_SET;

@Route(value = "users", layout = MainView.class)
@RolesAllowed({"ADMIN"})
public class UsersView extends VerticalLayout { // implements BeforeEnterObserver {

    @Inject
    private UserService userService;

    @Inject
    private RoleService roleService;

    @PostConstruct
    private void init() {

        TextField searchField = new TextField();
        searchField.setWidth("100%");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER.EAGER);

        Button newUser = new Button("New User");

        HorizontalLayout horizontalLayout = new HorizontalLayout(searchField, newUser);
        horizontalLayout.getStyle().set("max-width","700px");

        Grid<User> grid = new Grid<>(User.class, false);
        grid.setItems(userService.getAllUsers());
        Editor<User> editor = grid.getEditor();

        GridListDataView<User> dataView = grid.setItems(userService.getAllUsers());

        searchField.addValueChangeListener(e -> dataView.refreshAll());

        dataView.addFilter(user -> {
            String searchTerm = searchField.getValue().trim();

            if (searchTerm.isEmpty())
                return true;

            return matchesTerm(user.getUsername(),
                    searchTerm);
        });

        Grid.Column<User> removeColumn = grid.addComponentColumn(user -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), click -> {
                if (user.getId() == null) {
                    grid.setItems(userService.getAllUsers());
                } else {
                    userService.deleteUser(user.getId());
                    grid.setItems(userService.getAllUsers());
                }
                Notification.show("User deleted");
            });
            deleteButton.addClassName("delete-button");
            return deleteButton;
        }).setWidth("80px").setFlexGrow(0);

        Grid.Column<User> usernameColumn = grid
                .addColumn(User::getUsername)
                .setHeader("Username")
                .setAutoWidth(true).setFlexGrow(0);
        Grid.Column<User> passwordColumn = grid
                .addColumn(User::getPassword)
                .setRenderer(createUserRenderer())
                .setAutoWidth(true)
                .setHeader("Password");
        Grid.Column<User> rolesColumn = grid
                .addColumn(User::getRoles)
                .setWidth("250px")
                .setHeader("Roles");
        Grid.Column<User> editColumn = grid.addComponentColumn(person -> {
            Button editButton = new Button("Edit");
            editButton.addClickListener(e -> {
                if (editor.isOpen()) {
                    editor.cancel();
                }
                grid.getEditor().editItem(person);
            });
            return editButton;
        }).setWidth("150px").setFlexGrow(0);

        Binder<User> binder = new Binder<>(User.class);
        editor.setBinder(binder);
        editor.setBuffered(true);

        EmailField usernameField = new EmailField();
        usernameField.setWidthFull();
        binder.forField(usernameField).asRequired("Email must not be empty")
                .withValidator(new EmailValidator("Enter a valid email address"))
                .withStatusLabel(new Span("Username must not be empty"))
                .bind(User::getUsername, User::setUsername);
        usernameColumn.setEditorComponent(usernameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setWidthFull();
        binder.forField(passwordField).asRequired("Password must not be empty")
                .withStatusLabel(new Span("Password must not be empty"))
                .bind(User::getPassword, User::setPassword);
        passwordColumn.setEditorComponent(passwordField);

        MultiSelectComboBox<Role> rolesField = new MultiSelectComboBox<>();
        rolesField.setItems(roleService.getAllRoles());
        rolesField.setWidthFull();
        rolesField.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);
        binder.forField(rolesField).asRequired("Roles must not be empty")
                .withStatusLabel(new Span("Roles must not be empty"))
                .bind(User::getRoles, User::setRoles);
        rolesColumn.setEditorComponent(rolesField);

        Button saveButton = new Button("Save", e -> {
            if (usernameField.isInvalid() || passwordField.isEmpty() || passwordField.isInvalid() ||
                    rolesField.isEmpty() || rolesField.isInvalid()) {
                showErrorNotification("There are invalid fields. Please complete all required fields");
                return;
            }
            // Guardar el objeto editado en el Binder
            binder.writeBeanIfValid(editor.getItem());
            // Guardar en la base de datos
            userService.saveUser(editor.getItem()); // Actualiza la BD
            showInfoNotification("Data saved successfuly");
            // Cerrar el editor
            editor.save();
            //editor.closeEditor();
        });
        Button cancelButton = new Button(VaadinIcon.CLOSE.create(),
                e -> editor.cancel());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_ERROR);
        HorizontalLayout actions = new HorizontalLayout(saveButton,
                cancelButton);
        actions.setPadding(false);
        editColumn.setEditorComponent(actions);

        editor.addCancelListener(e -> {
            usernameField.setValue("");
            passwordField.setValue("");
            rolesField.setValue(EMPTY_SET);
        });

        // Listener para cuando se cierra el editor
        editor.addCloseListener(event -> {
            grid.setItems(userService.getAllUsers());
            grid.getDataProvider().refreshAll();
        });

        newUser.addClickListener(e -> {
            grid.getSelectionModel().deselectAll();
            User user = new User();
            List<User> users = userService.getAllUsers();
            users.add(user);
            grid.setItems(users);
            grid.getDataProvider().refreshAll();
            editor.editItem(user);
        });

        getThemeList().clear();
        getThemeList().add("spacing-s");
        add(horizontalLayout, grid);

        getStyle().set("max-width", "1000px");
    }

    private static Renderer<User> createUserRenderer() {
        return new ComponentRenderer<Component, User>(u -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            for (int i = 0; i < 8; ++i) {
                Component component = VaadinIcon.ASTERISK.create();
                component.getStyle().set("font-size", "4px");
                horizontalLayout.add(component);
            }
            return horizontalLayout;
        });
    }

    public UsersView() {

    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }

    public void showErrorNotification(String message) {
        // Crea un ícono de información
        Icon infoIcon = new Icon(VaadinIcon.INFO_CIRCLE);
        infoIcon.setColor("white");
        // Ajusta el tamaño del ícono (ej. 32px x 32px)
        infoIcon.setSize("32px");

        // Crea el mensaje de texto
        Span messageText = new Span(message);

        // Combina el ícono y el mensaje en un HorizontalLayout
        HorizontalLayout layout = new HorizontalLayout(infoIcon, messageText);
        layout.setAlignItems(Alignment.CENTER);  // Alinea el ícono y el texto verticalmente

        // Establece un ancho fijo para el layout
        layout.setWidth("300px");

        // Crea la notificación
        Notification notification = new Notification(layout);

        // Añade un tema de "contraste" para que se destaque como informativa
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        // Configura la duración en milisegundos
        notification.setDuration(3000);

        // Muestra la notificación en el centro de la pantalla
        notification.setPosition(Notification.Position.TOP_END);

        // Muestra la notificación
        notification.open();
    }

    public void showInfoNotification(String message) {
        // Crea un ícono de información
        Icon infoIcon = new Icon(VaadinIcon.INFO_CIRCLE);
        infoIcon.setColor("white");
        // Ajusta el tamaño del ícono (ej. 32px x 32px)
        infoIcon.setSize("32px");

        // Crea el mensaje de texto
        Span messageText = new Span(message);

        // Combina el ícono y el mensaje en un HorizontalLayout
        HorizontalLayout layout = new HorizontalLayout(infoIcon, messageText);
        layout.setAlignItems(Alignment.CENTER);  // Alinea el ícono y el texto verticalmente

        // Establece un ancho fijo para el layout
        layout.setWidth("300px");

        // Crea la notificación
        Notification notification = new Notification(layout);

        // Añade un tema de "contraste" para que se destaque como informativa
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);

        // Configura la duración en milisegundos
        notification.setDuration(3000);

        // Muestra la notificación en el centro de la pantalla
        notification.setPosition(Notification.Position.TOP_END);

        // Muestra la notificación
        notification.open();
    }

    /*
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        SecurityIdentity securityIdentity = (SecurityIdentity) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("securityIdentity");

        if (!securityIdentity.getRoles().contains("ADMIN")) {
            event.rerouteTo(LoginView.class);
        }
    }
    */

}
