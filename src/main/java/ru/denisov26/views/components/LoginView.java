package ru.denisov26.views.components;

import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.denisov26.security.AuthenticatedUser;

@AnonymousAllowed
@PageTitle("Авторизация")
@Route(value = "login")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;

    public LoginView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        LoginI18n i18n = new LoginI18n();
        LoginI18n.Form form = new LoginI18n.Form();
        form.setSubmit("Войти");
        form.setUsername("Введите email");
        form.setPassword("Пароль");
        form.setTitle("Авторизуйтесь");

        i18n.setForm(form);
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Tele2");
        i18n.getHeader().setDescription("Введите email и пароль");
        LoginI18n.ErrorMessage errorMessage = new LoginI18n.ErrorMessage();
        errorMessage.setMessage("Неправильная пара логин и пароль");
        i18n.setErrorMessage(errorMessage);


        setI18n(i18n);

        setForgotPasswordButtonVisible(false);
        setOpened(true);

        setForgotPasswordButtonVisible(false);
        setOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            setOpened(false);
            event.forwardTo("");
        }
        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
}
