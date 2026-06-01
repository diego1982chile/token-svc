package cl.dsoto.ui;

import cl.dsoto.model.Role;
import cl.dsoto.model.User;
import cl.dsoto.model.UserStatus;
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
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
import java.util.Set;

@Route(value = "users", layout = MainView.class)
@RolesAllowed({"ADMIN"})
public class UsersView extends VerticalLayout {

    @Inject
    private UserService userService;

    @Inject
    private RoleService roleService;

    private Grid.Column<User> passwordColumn;
    private Grid.Column<User> statusColumn;
    private Grid.Column<User> rolesColumn;
    private Grid.Column<User> detailsColumn;

    @PostConstruct
    void init() {
        configureView();

        TextField searchField = createSearchField();
        Button newUser = createNewUserButton();
        Grid<User> grid = createGrid(searchField, newUser);

        add(createHeading(), createToolbar(searchField, newUser), grid);
        expand(grid);
        addAttachListener(event -> event.getUI().getPage().executeJs(
                "const view = $0;" +
                        "const update = () => view.$server.setMobileLayout(window.innerWidth < 720);" +
                        "update();" +
                        "if (!view.__identityResizeHandler) {" +
                        "  view.__identityResizeHandler = update;" +
                        "  window.addEventListener('resize', update);" +
                        "}",
                getElement()));
        addDetachListener(event -> event.getUI().getPage().executeJs(
                "const view = $0;" +
                        "if (view.__identityResizeHandler) {" +
                        "  window.removeEventListener('resize', view.__identityResizeHandler);" +
                        "  delete view.__identityResizeHandler;" +
                        "}",
                getElement()));
    }

    @ClientCallable
    public void setMobileLayout(boolean mobile) {
        if (passwordColumn == null || statusColumn == null || rolesColumn == null || detailsColumn == null) {
            return;
        }
        passwordColumn.setVisible(!mobile);
        statusColumn.setVisible(!mobile);
        rolesColumn.setVisible(!mobile);
        detailsColumn.setVisible(mobile);
    }

    private void configureView() {
        addClassNames("alta-page", "alta-list-page");
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("gap", "var(--lumo-space-m)");
    }

    private VerticalLayout createHeading() {
        H1 title = new H1("Users");
        title.addClassNames("m-0");
        title.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("font-weight", "600");

        Paragraph description = new Paragraph("Review accounts, update passwords, and assign roles.");
        description.addClassNames("m-0", "text-secondary");

        VerticalLayout heading = new VerticalLayout(title, description);
        heading.addClassName("alta-page-heading");
        heading.setPadding(false);
        heading.setSpacing(false);
        return heading;
    }

    private HorizontalLayout createToolbar(TextField searchField, Button newUser) {
        HorizontalLayout toolbar = new HorizontalLayout(searchField, newUser);
        toolbar.addClassName("alta-toolbar");
        toolbar.setWidthFull();
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        toolbar.expand(searchField);
        return toolbar;
    }

    private TextField createSearchField() {
        TextField searchField = new TextField();
        searchField.addClassName("alta-search-field");
        searchField.setWidthFull();
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.setClearButtonVisible(true);
        return searchField;
    }

    private Button createNewUserButton() {
        Button newUser = new Button("New user", VaadinIcon.PLUS.create());
        newUser.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return newUser;
    }

    private Grid<User> createGrid(TextField searchField, Button newUser) {
        Grid<User> grid = new Grid<>(User.class, false);
        grid.addClassName("alta-grid");
        grid.setSizeFull();
        grid.setItems(userService.getAllUsers());
        Editor<User> editor = grid.getEditor();

        GridListDataView<User> dataView = grid.setItems(userService.getAllUsers());

        searchField.addValueChangeListener(e -> dataView.refreshAll());

        dataView.addFilter(user -> {
            String searchTerm = searchField.getValue().trim();

            if (searchTerm.isEmpty())
                return true;

            return matchesTerm(user.getUsername(), searchTerm)
                    || matchesTerm(getStatusLabel(user), searchTerm);
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
            deleteButton.addClassNames("alta-action-button", "delete-button");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
            deleteButton.getElement().setAttribute("title", "Delete user");
            return deleteButton;
        }).setHeader("").setWidth("64px").setFlexGrow(0);

        Grid.Column<User> usernameColumn = grid
                .addColumn(User::getUsername)
                .setHeader("Username")
                .setWidth("0").setFlexGrow(1);
        passwordColumn = grid
                .addColumn(User::getPassword)
                .setRenderer(createUserRenderer())
                .setHeader("Password")
                .setWidth("0").setFlexGrow(1);
        statusColumn = grid
                .addColumn(this::getStatusLabel)
                .setHeader("Status")
                .setWidth("0").setFlexGrow(1);
        rolesColumn = grid
                .addColumn(User::getRoles)
                .setHeader("Roles")
                .setWidth("0").setFlexGrow(1);
        detailsColumn = grid.addColumn(user -> getStatusLabel(user) + " - " + user.getRoles())
                .setHeader("Details")
                .setWidth("0").setFlexGrow(1);
        detailsColumn.setVisible(false);
        Grid.Column<User> editColumn = grid.addComponentColumn(person -> {
            Button editButton = new Button(VaadinIcon.PENCIL.create());
            editButton.addClassName("alta-action-button");
            editButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            editButton.getElement().setAttribute("title", "Edit user");
            editButton.addClickListener(e -> {
                if (editor.isOpen()) {
                    editor.cancel();
                }
                grid.getEditor().editItem(person);
            });
            return editButton;
        }).setHeader("Actions").setWidth("88px").setFlexGrow(0);

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

        Button saveButton = new Button(VaadinIcon.CHECK.create(), e -> {
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
        saveButton.addClassName("alta-action-button");
        saveButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        saveButton.getElement().setAttribute("title", "Save user");

        Button cancelButton = new Button(VaadinIcon.CLOSE.create(), e -> editor.cancel());
        cancelButton.addClassName("alta-action-button");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
        cancelButton.getElement().setAttribute("title", "Cancel edit");
        HorizontalLayout actions = new HorizontalLayout(saveButton,
                cancelButton);
        actions.addClassName("alta-grid-actions");
        actions.setPadding(false);
        editColumn.setEditorComponent(actions);

        editor.addCancelListener(e -> {
            usernameField.setValue("");
            passwordField.setValue("");
            rolesField.setValue(Set.of());
        });

        // Listener para cuando se cierra el editor
        editor.addCloseListener(event -> {
            grid.setItems(userService.getAllUsers());
            grid.getDataProvider().refreshAll();
        });

        newUser.addClickListener(e -> {
            grid.getSelectionModel().deselectAll();
            User user = new User();
            user.setStatus(UserStatus.PENDING);
            List<User> users = userService.getAllUsers();
            users.add(user);
            grid.setItems(users);
            grid.getDataProvider().refreshAll();
            editor.editItem(user);
        });

        return grid;
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
        if (value == null) {
            return false;
        }
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }

    private String getStatusLabel(User user) {
        if (user == null || user.getStatus() == null) {
            return UserStatus.ACTIVE.name();
        }
        return user.getStatus().name();
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

}
