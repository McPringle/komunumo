/*
 * Komunumo - Open Source Community Manager
 * Copyright (C) Marcus Fihlon and the individual contributors to Komunumo.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package app.komunumo.domain.user.boundary;

import app.komunumo.SecurityConfig;
import app.komunumo.domain.core.config.control.ConfigurationService;
import app.komunumo.domain.home.boundary.HomeView;
import app.komunumo.domain.user.control.LoginService;
import app.komunumo.infra.ui.vaadin.components.KomunumoMessageBox;
import app.komunumo.infra.ui.vaadin.layout.AbstractView;
import app.komunumo.infra.ui.vaadin.layout.WebsiteLayout;
import app.komunumo.util.NotificationUtil;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

@AnonymousAllowed
@Route(value = SecurityConfig.LOGIN_URL, layout = WebsiteLayout.class)
public final class LoginView extends AbstractView implements BeforeEnterObserver {

    private static final @NotNull Duration CONFIRMATION_TIMEOUT = Duration.ofMinutes(5);
    private static final @NotNull String CONFIRMATION_PATH = "/confirm";

    private final @NotNull LoginService loginService;
    private final @NotNull EmailField emailField;
    private final @NotNull Button submitButton;


    public LoginView(final @NotNull ConfigurationService configurationService,
                     final @NotNull LoginService loginService) {
        super(configurationService);
        this.loginService = loginService;

        setId("login-view");
        add(new H3(getTranslation("user.boundary.LoginView.login.title")));

        emailField = new EmailField();
        emailField.addClassName("email-field");
        emailField.setPlaceholder(getTranslation("user.boundary.LoginView.login.email.placeholder"));
        emailField.setRequired(true);
        emailField.setValueChangeMode(ValueChangeMode.EAGER);
        emailField.setClearButtonVisible(true);
        add(emailField);

        submitButton = new Button(getTranslation("user.boundary.LoginView.login.button"));
        submitButton.addClassName("email-button");
        submitButton.addThemeVariants(ButtonVariant.PRIMARY);
        submitButton.addClickListener(_ -> sendConfirmationMail());
        submitButton.addClickShortcut(Key.ENTER);
        add(submitButton);

        final var binder = new Binder<DummyBean>();
        binder.forField(emailField)
                .asRequired("")
                .withValidator(new EmailValidator(getTranslation("user.boundary.LoginView.login.email.validationError")))
                .bind(_ -> null, (_, _) -> { });
        binder.setBean(new DummyBean());
        binder.addStatusChangeListener(_ -> submitButton.setEnabled(binder.isValid()));
        binder.validate();

        emailField.focus();
    }

    private void sendConfirmationMail() {
        emailField.setEnabled(false);
        submitButton.setEnabled(false);

        final var email = emailField.getValue().trim();
        final var locale = getLocale();

        loginService.startLoginProcess(email, locale);

        add(new KomunumoMessageBox(getTranslation("user.boundary.LoginView.confirm.description", email, CONFIRMATION_TIMEOUT.toMinutes()),
                KomunumoMessageBox.MessageType.INFO));
    }

    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation(UI.getCurrent().getLocale(), "user.boundary.LoginView.title");
    }

    @Override
    public void beforeEnter(final @NotNull BeforeEnterEvent beforeEnterEvent) {
        redirectUserWhenLoggedIn(beforeEnterEvent);
        verifyConfirmationLink(beforeEnterEvent);
    }

    private void redirectUserWhenLoggedIn(final @NotNull BeforeEnterEvent beforeEnterEvent) {
        final var loggedInUser = loginService.getLoggedInUser();
        if (loggedInUser.isPresent()) {
            beforeEnterEvent.forwardTo(HomeView.class, QueryParameters.empty());
        }
    }

    private void verifyConfirmationLink(final @NotNull BeforeEnterEvent beforeEnterEvent) {
        beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault(LoginService.CONFIRMATION_PARAMETER, List.of())
                .stream()
                .findFirst()
                .ifPresent(confirmationId -> {
                    if (loginService.handleLogin(confirmationId)) {
                        beforeEnterEvent.forwardTo(HomeView.class, QueryParameters.empty());
                        NotificationUtil.showNotification(
                                getTranslation("user.boundary.LoginView.success.message"),
                                NotificationVariant.SUCCESS);
                    } else {
                        add(new KomunumoMessageBox(getTranslation("user.boundary.LoginView.error.message"),
                                KomunumoMessageBox.MessageType.ERROR));
                    }
                });
    }

    @SuppressWarnings("java:S2094") // DummyBean for Binder (to use validation only)
    private static final class DummyBean { }
}
