package cl.dsoto.ui;

import cl.dsoto.model.Role;
import cl.dsoto.services.RoleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

import java.util.Set;

@Route(value = "roles", layout = MainView.class)
@RolesAllowed({"ADMIN"})
public class RolesView extends VerticalLayout {

    @Inject
    private RoleService roleService;

    @PostConstruct
    void init() {
        configureView();

        TextField searchField = createSearchField();
        Button newRole = createNewRoleButton();
        Grid<Role> grid = createGrid(searchField, newRole);

        add(createHeading(), createToolbar(searchField, newRole), grid);
        expand(grid);
    }

    private void configureView() {
        addClassNames("alta-page", "alta-list-page");
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("gap", "var(--lumo-space-m)");
    }

    private VerticalLayout createHeading() {
        H1 title = new H1("Roles");
        title.addClassNames("m-0");
        title.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("font-weight", "600");

        Paragraph description = new Paragraph("Maintain reusable access groups for the application.");
        description.addClassNames("m-0", "text-secondary");

        VerticalLayout heading = new VerticalLayout(title, description);
        heading.addClassName("alta-page-heading");
        heading.setPadding(false);
        heading.setSpacing(false);
        return heading;
    }

    private HorizontalLayout createToolbar(TextField searchField, Button newRole) {
        HorizontalLayout toolbar = new HorizontalLayout(searchField, newRole);
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

    private Button createNewRoleButton() {
        Button newRole = new Button("New role", VaadinIcon.PLUS.create());
        newRole.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return newRole;
    }

    private Grid<Role> createGrid(TextField searchField, Button newRole) {
        Grid<Role> grid = new Grid<>(Role.class, false);
        grid.addClassName("alta-grid");
        grid.setSizeFull();
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
            deleteButton.addClassNames("alta-action-button", "delete-button");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
            deleteButton.getElement().setAttribute("title", "Delete role");
            return deleteButton;
        }).setHeader("").setWidth("64px").setFlexGrow(0);

        Grid.Column<Role> rolenameColumn = grid
                .addColumn(Role::getRolename)
                .setHeader("Rolename")
                .setWidth("0").setFlexGrow(1);
        Grid.Column<Role> editColumn = grid.addComponentColumn(role -> {
            Button editButton = new Button(VaadinIcon.PENCIL.create());
            editButton.addClassName("alta-action-button");
            editButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            editButton.getElement().setAttribute("title", "Edit role");
            editButton.addClickListener(e -> {
                if (editor.isOpen()) {
                    editor.cancel();
                }
                grid.getEditor().editItem(role);
            });
            return editButton;
        }).setHeader("Actions").setWidth("88px").setFlexGrow(0);

        Binder<Role> binder = new Binder<>(Role.class);
        editor.setBinder(binder);
        editor.setBuffered(true);

        TextField rolenameField = new TextField();
        rolenameField.setWidthFull();
        binder.forField(rolenameField).asRequired("Rolename must not be empty")
                .withStatusLabel(new Span("Rolename must not be empty"))
                .bind(Role::getRolename, Role::setRolename);
        rolenameColumn.setEditorComponent(rolenameField);

        Button saveButton = new Button(VaadinIcon.CHECK.create(), e -> {
            if (rolenameField.getValue() == null || rolenameField.getValue().trim().isEmpty()
                    || !binder.writeBeanIfValid(editor.getItem())) {
                Notification.show("Rolename must not be empty");
                return;
            }

            Role role = editor.getItem();
            role.setRolename(role.getRolename().trim());
            roleService.saveRole(role);

            Notification.show("Role saved");
            editor.save();
            editor.closeEditor();
        });
        saveButton.addClassName("alta-action-button");
        saveButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        saveButton.getElement().setAttribute("title", "Save role");

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
        return grid;
    }

    public RolesView() {

    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }

}
