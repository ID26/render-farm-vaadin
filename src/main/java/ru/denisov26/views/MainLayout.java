package ru.denisov26.views;


import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import ru.denisov26.components.appnav.AppNav;
import ru.denisov26.components.appnav.AppNavItem;
import ru.denisov26.domain.User;
import ru.denisov26.security.AuthenticatedUser;
import ru.denisov26.views.components.EditUserView;
import ru.denisov26.views.components.RegistrationView;
import ru.denisov26.views.components.TasksView;

import java.util.Optional;

public class MainLayout extends AppLayout {

    private H2 viewTitle;
    private final AccessAnnotationChecker accessChecker;
    private final AuthenticatedUser authenticatedUser;

    public MainLayout(AccessAnnotationChecker accessChecker, AuthenticatedUser authenticatedUser) {
        this.accessChecker = accessChecker;
        this.authenticatedUser = authenticatedUser;
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        H1 appName = new H1("Рендер ферма");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private AppNav createNavigation() {
        AppNav nav = new AppNav();

        nav.addItem(new AppNavItem("Список задач", TasksView.class, "la la-globe"));

        if (accessChecker.hasAccess(RegistrationView.class) && this.authenticatedUser.get().isEmpty()) {
            nav.addItem(new AppNavItem("Регистрация", RegistrationView.class, "la la-user"));
        }
        if (accessChecker.hasAccess(EditUserView.class) && this.authenticatedUser.get().isPresent()) {
            nav.addItem(new AppNavItem("Редактировать профиль", EditUserView.class, "la la-user"));
        }


        return nav;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(String.format("%s %s", user.getFirstName(), user.getLastName()));

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getEmail());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Выйти", e -> {
                authenticatedUser.logout();
            });

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Авторизоваться");
            layout.add(loginLink);
        }

        return layout;
    }
}
