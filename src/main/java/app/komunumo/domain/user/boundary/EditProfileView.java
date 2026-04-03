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
import app.komunumo.domain.core.layout.boundary.WebsiteLayout;
import app.komunumo.domain.user.control.LoginService;
import app.komunumo.domain.user.control.UserService;
import app.komunumo.domain.user.entity.UserDto;
import app.komunumo.vaadin.components.AbstractView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

@Route(value = "settings/profile", layout = WebsiteLayout.class)
@RolesAllowed("USER_LOCAL")
public class EditProfileView extends AbstractView {

    private static final int BIO_MAX_LENGTH = 1_000;

    private final @NotNull LoginService loginService;
    private final @NotNull UserService userService;

    /**
     * <p>Creates a new view instance with access to the configuration service for
     * retrieving localized configuration values such as the instance name.</p>
     *
     * @param configurationService the configuration service used to resolve the instance name;
     *                             must not be {@code null}
     */
    public EditProfileView(final @NotNull ConfigurationService configurationService,
                           final @NotNull LoginService loginService,
                           final @NotNull UserService userService) {
        super(configurationService);
        this.loginService = loginService;
        this.userService = userService;
        createUserInterface();
    }

    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation("user.boundary.EditProfileView.title");
    }

    private void createUserInterface() {
        final var user = loginService.getLoggedInUser()
                .orElseThrow(() -> new IllegalStateException("No logged-in user"));

        final var emailField = new EmailField(getTranslation("user.boundary.EditProfileView.email"));
        emailField.setReadOnly(true);
        emailField.setValueChangeMode(EAGER);
        emailField.setWidthFull();

        final var nameField = new TextField(getTranslation("user.boundary.EditProfileView.name"));
        nameField.setRequiredIndicatorVisible(true);
        nameField.setValueChangeMode(EAGER);
        nameField.setWidthFull();

        final var bioField = new TextArea(getTranslation("user.boundary.EditProfileView.bio"));
        bioField.setWidthFull();
        bioField.setMaxLength(BIO_MAX_LENGTH);
        bioField.setValueChangeMode(EAGER);
        bioField.addValueChangeListener(event -> {
            final int length = event.getValue().length();
            bioField.setHelperText(length + " / " + BIO_MAX_LENGTH);
        });

        final var binder = new Binder<EditProfileFormData>();

        binder.forField(emailField)
                .asRequired(getTranslation("user.boundary.EditProfileView.email.required"))
                .withConverter(String::trim, String::trim)
                .bind(EditProfileFormData::email, EditProfileFormData::setEmail);

        binder.forField(nameField)
                .asRequired(getTranslation("user.boundary.EditProfileView.name.required"))
                .withConverter(String::trim, String::trim)
                .bind(EditProfileFormData::name, EditProfileFormData::setName);

        binder.forField(bioField)
                .bind(EditProfileFormData::bio, EditProfileFormData::setBio);

        final var formData = new EditProfileFormData(user);
        binder.setBean(formData);

        final var saveButton = new Button(getTranslation("user.boundary.EditProfileView.save"));
        saveButton.addClickListener(_ -> {
            if (!binder.validate().isOk()) {
                return;
            }

            final var updatedUser = new UserDto(
                    user.id(),
                    user.created(),
                    user.updated(),
                    user.profile(),
                    formData.email().isBlank() ? null : formData.email(),
                    formData.name(),
                    formData.bio(),
                    user.imageId(),
                    user.role(),
                    user.type()
            );

            userService.storeUser(updatedUser);
        });

        final var formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setMaxWidth("40rem");
        formLayout.add(emailField, nameField, bioField);

        add(formLayout, saveButton);
    }

    private static final class EditProfileFormData {

        private String email;
        private String name;
        private String bio;

        public EditProfileFormData(final @NotNull UserDto user) {
            setEmail(Optional.ofNullable(user.email()).orElse(""));
            setName(user.name());
            setBio(user.bio());
        }

        public String email() {
            return email;
        }

        public void setEmail(final @NotNull String email) {
            this.email = email;
        }

        public String name() {
            return name;
        }

        public void setName(final @NotNull String name) {
            this.name = name;
        }

        public String bio() {
            return bio;
        }

        public void setBio(final @NotNull String bio) {
            this.bio = bio;
        }
    }
}
