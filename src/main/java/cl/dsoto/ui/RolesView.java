package cl.dsoto.ui;

import cl.dsoto.entities.Role;
import cl.dsoto.entities.User;
import cl.dsoto.services.RoleService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

import java.util.Set;

@Route(value = "roles", layout = MainView.class)
@RolesAllowed({"ADMIN"})
public class RolesView extends VerticalLayout { // implements BeforeEnterObserver {

    @Inject
    private RoleService roleService;

    @PostConstruct
    private void init() {

        TextField searchField = new TextField();
        searchField.setWidth("100%");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER.EAGER);

        Button newRole = new Button("New Role");

        HorizontalLayout horizontalLayout = new HorizontalLayout(searchField, newRole);
        horizontalLayout.getStyle().set("max-width","700px");

        Grid<Role> grid = new Grid<>(Role.class, false);
        grid.setItems(roleService.getAllRoles());
        Editor<Role> editor = grid.getEditor();

        GridListDataView<Role> dataView = grid.setItems(roleService.getAllRoles());

        searchField.addValueChangeListener(e -> dataView.refreshAll());

        dataView.addFilter(role -> {
            String searchTerm = searchField.getValue().trim();

            if (searchTerm.isEmpty())
                return true;

            return matchesTerm(role.getRolename(),
                    searchTerm);
        });

        Grid.Column<Role> removeColumn = grid.addComponentColumn(role -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), click -> {
                if (role.getId() == null) {
                    grid.setItems(roleService.getAllRoles());
                } else {
                    roleService.deleteRole(role.getId());
                    grid.setItems(roleService.getAllRoles());
                }
                Notification.show("Role deleted");
            });
            deleteButton.addClassName("delete-button");
            return deleteButton;
        }).setWidth("80px").setFlexGrow(0);

        Grid.Column<Role> rolenameColumn = grid
                .addColumn(Role::getRolename)
                .setHeader("Rolename")
                .setWidth("200px").setFlexGrow(0);
        Grid.Column<Role> editColumn = grid.addComponentColumn(role -> {
            Button editButton = new Button("Edit");
            editButton.addClickListener(e -> {
                if (editor.isOpen()) {
                    editor.cancel();
                }
                grid.getEditor().editItem(role);
            });
            return editButton;
        }).setWidth("150px").setFlexGrow(0);

        Binder<Role> binder = new Binder<>(Role.class);
        editor.setBinder(binder);
        editor.setBuffered(true);

        TextField rolenameField = new TextField();
        rolenameField.setWidthFull();
        binder.forField(rolenameField).asRequired("Rolename must not be empty")
                .withStatusLabel(new Span("Rolename must not be empty"))
                .bind(Role::getRolename, Role::setRolename);
        rolenameColumn.setEditorComponent(rolenameField);

        Button saveButton = new Button("Save", e -> {
            //if (editor.isOpen()) {
            // Guardar el objeto editado en el Binder
            Long previousRolename = editor.getItem().getId();
            binder.writeBeanIfValid(editor.getItem());
            Role role = editor.getItem();
            // Guardar en la base de datos
            /*
            if (previousRolename == null) {
                roleService.saveRole(role); // Actualiza la BD
            } else {
                role.setPreviousRolename(previousRolename);
                roleService.updateRole(role); // Actualiza la BD
            }
            */
            //role.setPreviousRolename(previousRolename);
            roleService.saveRole(role);

            Notification.show("Datos guardados en la base de datos");
            // Cerrar el editor
            editor.save();
            editor.closeEditor();
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
            rolenameField.setValue("");
        });

        // Listener para cuando se cierra el editor
        editor.addCloseListener(event -> {
            grid.setItems(roleService.getAllRoles());
            grid.getDataProvider().refreshAll();
        });

        newRole.addClickListener(e -> {
            grid.getSelectionModel().deselectAll();
            Role role = new Role();
            Set<Role> roles = roleService.getAllRoles();
            roles.add(role);
            grid.setItems(roles);
            grid.getDataProvider().refreshAll();
            editor.editItem(role);
        });

        getThemeList().clear();
        getThemeList().add("spacing-s");
        add(horizontalLayout, grid);

        getStyle().set("max-width", "500px");
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

    public RolesView() {

    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
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
