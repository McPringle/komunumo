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

import app.komunumo.domain.core.config.control.ConfigurationService;
import app.komunumo.domain.core.confirmation.control.ConfirmationService;
import app.komunumo.domain.home.boundary.HomeView;
import app.komunumo.domain.user.control.LoginService;
import app.komunumo.domain.user.control.RegistrationService;
import app.komunumo.infra.ui.vaadin.layout.AbstractView;
import app.komunumo.infra.ui.vaadin.layout.WebsiteLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static app.komunumo.domain.core.config.entity.ConfigurationSetting.INSTANCE_REGISTRATION_ALLOWED;

@Route(value = "register", layout = WebsiteLayout.class)
@AnonymousAllowed
public final class UserRegistrationView extends AbstractView implements BeforeEnterObserver {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(UserRegistrationView.class);

    private final @NotNull ConfigurationService configurationService;
    private final @NotNull RegistrationService registrationService;
    private final @NotNull ConfirmationService confirmationService;
    private final @NotNull LoginService loginService;

    public UserRegistrationView(final @NotNull ConfigurationService configurationService,
                                   final @NotNull RegistrationService registrationService,
                                   final @NotNull ConfirmationService confirmationService,
                                   final @NotNull LoginService loginService) {
        super(configurationService);
        this.configurationService = configurationService;
        this.registrationService = registrationService;
        this.confirmationService = confirmationService;
        this.loginService = loginService;
    }

    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation("user.boundary.UserRegistrationView.title");
    }

    @Override
    public void beforeEnter(final @NotNull BeforeEnterEvent beforeEnterEvent) {
        if (!configurationService.getConfiguration(INSTANCE_REGISTRATION_ALLOWED, Boolean.class)) {
            LOGGER.warn("Registration attempt while registration is disabled.");
            beforeEnterEvent.forwardTo(HomeView.class);
        }

        final var loggedInUser = loginService.getLoggedInUser();
        if (loggedInUser.isPresent()) {
            beforeEnterEvent.forwardTo(HomeView.class);
        } else {
            createUserInterface();
        }
    }

    private void createUserInterface() {
        addClassName("user-registration-view");

        add(new H2(getTranslation("user.boundary.UserRegistrationView.welcome")));
        add(new Paragraph(getTranslation("user.boundary.UserRegistrationView.description")));

        final var nameField = new TextField(getTranslation("user.boundary.UserRegistrationView.name.label"));
        nameField.addClassName("name-field");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setMinLength(3);
        nameField.setPlaceholder(getTranslation("user.boundary.UserRegistrationView.name.placeholder"));
        nameField.setValueChangeMode(ValueChangeMode.EAGER);
        nameField.setWidthFull();

        final var emailField = new EmailField(getTranslation("user.boundary.UserRegistrationView.email.label"));
        emailField.addClassName("email-field");
        emailField.setRequiredIndicatorVisible(true);
        emailField.setPlaceholder(getTranslation("user.boundary.UserRegistrationView.email.placeholder"));
        emailField.setValueChangeMode(ValueChangeMode.EAGER);
        emailField.setWidthFull();

        final var registerButton = new Button(getTranslation("user.boundary.UserRegistrationView.registerButton"));
        registerButton.addClassName("register-button");
        registerButton.setEnabled(false);
        registerButton.addClickListener(_ -> {
            final var name = nameField.getValue();
            final var email = emailField.getValue();
            List.of(nameField, emailField, registerButton).forEach(field -> field.setEnabled(false));

            registrationService.startRegistrationProcess(name, email, getLocale());

            final var dialog = new ConfirmDialog();
            dialog.addClassName("registration-confirmation-dialog");
            dialog.setHeader(getTranslation("user.boundary.UserRegistrationView.confirmationDialog.header"));
            dialog.setText(getTranslation("user.boundary.UserRegistrationView.confirmationDialog.text",
                    email, confirmationService.getConfirmationTimeoutText(getLocale())));
            dialog.setConfirmText(getTranslation("user.boundary.UserRegistrationView.confirmationDialog.confirmButton"));
            dialog.addConfirmListener(_ -> {
                dialog.close();
                UI.getCurrent().navigate(HomeView.class);
            });
            dialog.open();
        });

        List.of(nameField, emailField).forEach(field -> field.addValueChangeListener(
                _ -> registerButton.setEnabled(
                        !nameField.isInvalid() && !emailField.isEmpty() && !emailField.isInvalid())));

        add(nameField, emailField, registerButton);

        nameField.focus();
    }
}
