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
package app.komunumo.domain.community.boundary;

import app.komunumo.domain.user.entity.UserRole;
import app.komunumo.test.KaribuTest;
import app.komunumo.vaadin.components.ProfileField;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Test;

import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateCommunityViewKT extends KaribuTest {

    @Test
    void testComponent() {
        login(getTestUser(UserRole.USER));

        UI.getCurrent().navigate(CreateCommunityView.class);
        var container = _get(VerticalLayout.class, spec -> spec.withClasses("community-add-component"));
        assertThat(container).isNotNull();

        var profileField = _get(ProfileField.class, spec -> spec.withClasses("profile-field"));
        assertThat(profileField).isNotNull();

        var nameField = _get(TextField.class, spec -> spec.withClasses("name-field"));
        assertThat(nameField).isNotNull();

        var descriptionField = _get(TextArea.class, spec -> spec.withClasses("description-field"));
        assertThat(descriptionField).isNotNull();

        var createButton = _get(Button.class, spec -> spec.withClasses("create-button"));
        assertThat(createButton).isNotNull();
    }

    @Test
    void testCreate_invalidSubmission() {
        login(getTestUser(UserRole.USER));
        UI.getCurrent().navigate(CreateCommunityView.class);

        CreateCommunityComponent component = _get(CreateCommunityComponent.class);
        var createButton = _get(Button.class, spec -> spec.withClasses("create-button"));
        createButton.click();

        assertThat(component.getBinder().isValid()).isFalse();
    }

    @Test
    void testCreate_successfulSubmission() {
        login(getTestUser(UserRole.USER));
        UI.getCurrent().navigate(CreateCommunityView.class);

        CreateCommunityComponent component = _get(CreateCommunityComponent.class);
        var profile = _get(ProfileField.class, spec -> spec.withClasses("profile-field"));
        var name = _get(TextField.class, spec -> spec.withClasses("name-field"));
        var createButton = _get(Button.class, spec -> spec.withClasses("create-button"));

        profile.setValue("@testCommunity");
        name.setValue("Test Community");
        assertThat(component.getBinder().isValid()).isTrue();

        createButton.click();

        assertThat(UI.getCurrent().getActiveViewLocation().getPath())
                .isEqualTo("communities/@testCommunity");
        assertThat(_get(H2.class, spec -> spec.withClasses("community-name")).getText())
                .isEqualTo("Test Community");
        assertThat(_get(Paragraph.class, spec -> spec.withClasses("community-profile")).getText())
                .isEqualTo("@testCommunity");
    }

    @Test
    void sessionTimeout() {
        login(getTestUser(UserRole.USER));
        UI.getCurrent().navigate(CreateCommunityView.class);

        CreateCommunityComponent component = _get(CreateCommunityComponent.class);
        var profile = _get(ProfileField.class, spec -> spec.withClasses("profile-field"));
        var name = _get(TextField.class, spec -> spec.withClasses("name-field"));
        var createButton = _get(Button.class, spec -> spec.withClasses("create-button"));

        profile.setValue("Test Profile");
        name.setValue("Test Name");
        assertThat(component.getBinder().isValid()).isTrue();

        logout(); // simulate session timeout by logging out

        createButton.click();

        final var notification = _get(Notification.class);
        assertThat(notification.isOpened()).isTrue();

        final var notificationDiv = _get(notification, Div.class);
        assertThat(notificationDiv.getText()).startsWith("You do not have permission to create a new community.");
    }

}
