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

import app.komunumo.domain.user.control.UserService;
import app.komunumo.domain.user.entity.UserRole;
import app.komunumo.test.KaribuTest;
import app.komunumo.vaadin.components.MarkdownEditor;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

class EditProfileViewKT extends KaribuTest {

    @Autowired
    private UserService userService;

    @Test
    void editProfileFlow() {
        final var testUser = getTestUser(UserRole.USER);
        assertThat(testUser).isNotNull();
        assertThat(testUser.id()).isNotNull();
        userService.getUserById(testUser.id()).ifPresentOrElse(
                user -> {
                    assertThat(user.email()).isEqualTo(testUser.email());
                    assertThat(user.name()).isEqualTo(testUser.name());
                    assertThat(user.bio()).isEqualTo(testUser.bio());
                },
                () -> {
                    throw new RuntimeException("Test user not found");
                }
        );

        login(testUser);

        UI.getCurrent().navigate(EditProfileView.class);
        final var view = _get(VerticalLayout.class, spec -> spec.withClasses("edit-profile-view"));
        assertThat(view).isNotNull();

        final var emailField = _get(EmailField.class, spec -> spec.withClasses("email-field"));
        assertThat(emailField).isNotNull();
        assertThat(emailField.getValue()).isEqualTo(testUser.email());
        assertThat(emailField.isReadOnly()).isTrue();

        final var nameField = _get(TextField.class, spec -> spec.withClasses("name-field"));
        assertThat(nameField).isNotNull();
        assertThat(nameField.getValue()).isEqualTo(testUser.name());
        assertThat(nameField.isReadOnly()).isFalse();

        final var bioField = _get(MarkdownEditor.class, spec -> spec.withClasses("bio-field"));
        assertThat(bioField).isNotNull();
        assertThat(bioField.getValue()).isEqualTo(testUser.bio());
        assertThat(bioField.isReadOnly()).isFalse();

        final var saveButton = _get(Button.class, spec -> spec.withClasses("save-button"));
        assertThat(saveButton).isNotNull();
        assertThat(saveButton.isEnabled()).isTrue();

        assertThat(nameField.getErrorMessage()).isEmpty();
        nameField.setValue("");
        assertThat(nameField.getErrorMessage()).isEqualTo("Name is required");

        saveButton.click();
        userService.getUserById(testUser.id()).ifPresentOrElse(
                user -> {
                    assertThat(user.email()).isEqualTo(testUser.email());
                    assertThat(user.name()).isEqualTo(testUser.name());
                    assertThat(user.bio()).isEqualTo(testUser.bio());
                },
                () -> {
                    throw new RuntimeException("Test user not found");
                }
        );

        nameField.setValue("Modified Name");
        bioField.setValue("Modified Bio");
        saveButton.click();

        final var notification = _get(Notification.class);
        assertThat(notification.isOpened()).isTrue();

        final var notificationText = notification.getElement().getProperty("text");
        assertThat(notificationText).isEqualTo("Your profile has been successfully updated.");

        userService.getUserById(testUser.id()).ifPresentOrElse(
                user -> {
                    assertThat(user.email()).isEqualTo(testUser.email());
                    assertThat(user.name()).isEqualTo("Modified Name");
                    assertThat(user.bio()).isEqualTo("Modified Bio");
                },
                () -> {
                    throw new RuntimeException("Test user not found");
                }
        );
    }

}
