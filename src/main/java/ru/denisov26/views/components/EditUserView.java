package ru.denisov26.views.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.DateRangeValidator;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ru.denisov26.domain.User;
import ru.denisov26.security.AuthenticatedUser;
import ru.denisov26.services.UserService;
import ru.denisov26.views.MainLayout;

import javax.annotation.security.PermitAll;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@PageTitle("Редактирование пользователя")
@Route(value = "render-farm/edit-user-form", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class EditUserView extends Div {
    private final UserService userService;
    private final AuthenticatedUser authenticatedUser;
    private TextField firstName = new TextField("Имя");
    private TextField lastName = new TextField("Фамилия");
    private TextField patronymic = new TextField("Отчество");
    private DatePicker dateOfBirth = new DatePicker("Дата рождения");
    private EmailField email = new EmailField("Email");
    private TextField password = new TextField("Пароль");
    private Button cancel = new Button("Отмена");
    private Button save = new Button("Сохранить");

    private Binder<User> binder = new Binder<>(User.class);

    public EditUserView(UserService userService, AuthenticatedUser authenticatedUser) {
        this.userService = userService;
        this.authenticatedUser = authenticatedUser;

        addClassName("edit-user-view");

        add(createTitle());
        add(createFormLayout());
        add(createButtonLayout());
        buildBinderFields();

        binder.bindInstanceFields(this);
        clearForm();
        fillTextField();

        cancel.addClickListener(e -> {
            clearForm();
            fillTextField();
        });
        save.addClickListener(e -> {
            if (binder.isValid()) {
                User user = binder.getBean();
                user.setId(authenticatedUser.get().orElseThrow(() -> new RuntimeException("Не удается получить id пользоватяля")).getId());
                userService.update(user);
                Notification.show(binder.getBean().getClass().getSimpleName() + " изменен успешно")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                clearForm();
                getUI().ifPresent(ui -> ui.navigate("/login"));
            }
        });
        save.addClickShortcut(Key.ENTER);
    }

    private void fillTextField() {
        Optional<User> userOpt = authenticatedUser.get();
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            this.firstName.setValue(!Objects.isNull(user.getFirstName()) ? user.getFirstName() : "");
            this.lastName.setValue(!Objects.isNull(user.getLastName()) ? user.getLastName() : "");
            this.patronymic.setValue(!Objects.isNull(user.getPatronymic()) ? user.getPatronymic() : "");
            this.dateOfBirth.setValue(!Objects.isNull(user.getDateOfBirth()) ? user.getDateOfBirth() : LocalDate.EPOCH);
            this.email.setValue(!Objects.isNull(user.getEmail()) ? user.getEmail() : "");
            this.password.setValue("");
        }
    }

    private void clearForm() {
        binder.setBean(new User());
    }

    private Component createTitle() {
        return new H3("Личные данные");
    }

    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();
        setFormLayoutField();
        formLayout.add(firstName, lastName, patronymic, dateOfBirth, email, password);
        return formLayout;
    }

    private Component createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName("button-layout");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save);
        buttonLayout.add(cancel);
        return buttonLayout;
    }
    private void setFormLayoutField() {

        email.getElement().setAttribute("name", "email");
        email.setPlaceholder("username@example.com");
        email.setClearButtonVisible(true);
        email.setPattern("^.+@.+\\..+$");

        firstName.getElement().setAttribute("name", "firstName");
        firstName.setPlaceholder("Имя");
        firstName.setClearButtonVisible(true);
        firstName.setPattern("^[a-zA-Zа-яА-Я]{2,255}$");

        lastName.getElement().setAttribute("name", "lastName");
        lastName.setPlaceholder("Фамилия");
        lastName.setClearButtonVisible(true);
        lastName.setPattern("^[a-zA-Zа-яА-Я]{2,255}$");

        patronymic.getElement().setAttribute("name", "patronymic");
        patronymic.setPlaceholder("Отчество");
        patronymic.setClearButtonVisible(true);
        patronymic.setPattern("^[a-zA-Zа-яА-Я]{0,255}$");

        dateOfBirth.getElement().setAttribute("name", "dateOfBirth");
        dateOfBirth.setPlaceholder("Дата рождения");
        dateOfBirth.setClearButtonVisible(true);
        dateOfBirth.setRequired(true);

        password.getElement().setAttribute("name", "password");
        password.setPlaceholder("Пароль");
        password.setClearButtonVisible(true);
        password.setPattern(".+");
    }

    private void buildBinderFields() {
        binder.bindInstanceFields(this);
        binder.forField(email).withValidator(new EmailValidator("Введите коррекный email")).bind(User::getEmail, User::setEmail);
        binder.forField(firstName).withValidator(new RegexpValidator("Введите имя 2 - 255 буквенных символов", "^[a-zA-Zа-яА-Я]{2,255}$")).bind(User::getFirstName, User::setFirstName);
        binder.forField(lastName).withValidator(new RegexpValidator("Введите имя 2 - 255 буквенных символов", "^[a-zA-Zа-яА-Я]{2,255}$")).bind(User::getLastName, User::setLastName);
        binder.forField(patronymic).withValidator(new RegexpValidator("Введите имя 0 - 255 буквенных символов", "^[a-zA-Zа-яА-Я]{0,255}$")).bind(User::getPatronymic, User::setPatronymic);
        binder.forField(dateOfBirth).withValidator(new DateRangeValidator("Пользователь должен быть старше 14 лет", LocalDate.of(1900, 1, 1), LocalDate.now().minusYears(14L))).bind(User::getDateOfBirth, User::setDateOfBirth);
        binder.forField(password).withValidator(new StringLengthValidator("Пароль должен быть от 6 до 25 символов включительно", 6, 25)).bind(User::getPassword, User::setPassword);
    }
}
