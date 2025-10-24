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
package app.komunumo.ui.views.community;

import app.komunumo.data.dto.UserRole;
import app.komunumo.ui.KaribuTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Test;

import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author smzoha
 * @since 20/10/25
 **/
public class CommunityAddViewKT extends KaribuTest {

    @Test
    void testComponent() {
        login(getTestUser(UserRole.USER));

        UI.getCurrent().navigate("communities/add");
        var container = _get(VerticalLayout.class, spec -> spec.withClasses("community-add-component"));
        assertThat(container).isNotNull();

        assertThat(container.getChildren()
                .filter(child -> child instanceof TextField).count()).isEqualTo(2);

        assertThat(container.getChildren()
                .filter(child -> child instanceof TextArea).count()).isEqualTo(1);

        assertThat(container.getChildren()
                .filter(child -> child instanceof Button).count()).isEqualTo(1);

        var profileField = _get(TextField.class, spec -> spec.withClasses("profile-field"));
        assertThat(profileField).isNotNull();

        var nameField = _get(TextField.class, spec -> spec.withClasses("name-field"));
        assertThat(nameField).isNotNull();

        var descriptionField = _get(TextArea.class, spec -> spec.withClasses("description-field"));
        assertThat(descriptionField).isNotNull();

        var saveButton = _get(Button.class, spec -> spec.withClasses("save-button"));
        assertThat(saveButton).isNotNull();
    }

    @Test
    void testSave_invalidSubmission() {
        login(getTestUser(UserRole.USER));
        UI.getCurrent().navigate("communities/add");

        CommunityAddComponent component = _get(CommunityAddComponent.class);
        var saveButton = _get(Button.class, spec -> spec.withClasses("save-button"));
        saveButton.click();

        assertThat(component.getBinder().isValid()).isFalse();
    }

    @Test
    void testSave_successfulSubmission() {
        login(getTestUser(UserRole.USER));
        UI.getCurrent().navigate("communities/add");

        CommunityAddComponent component = _get(CommunityAddComponent.class);
        var profile = _get(TextField.class, spec -> spec.withClasses("profile-field"));
        var name = _get(TextField.class, spec -> spec.withClasses("name-field"));
        var saveButton = _get(Button.class, spec -> spec.withClasses("save-button"));

        profile.setValue("Test Profile");
        name.setValue("Test Name");
        assertThat(component.getBinder().isValid()).isTrue();

        saveButton.click();

        assertThat(UI.getCurrent().getActiveViewLocation().getPath()).isEqualTo("communities");
    }

    @Test
    void sessionTimeout() {
        login(getTestUser(UserRole.USER));
        UI.getCurrent().navigate("communities/add");

        CommunityAddComponent component = _get(CommunityAddComponent.class);
        var profile = _get(TextField.class, spec -> spec.withClasses("profile-field"));
        var name = _get(TextField.class, spec -> spec.withClasses("name-field"));
        var saveButton = _get(Button.class, spec -> spec.withClasses("save-button"));

        profile.setValue("Test Profile");
        name.setValue("Test Name");
        assertThat(component.getBinder().isValid()).isTrue();

        logout(); // simulate session timeout by logging out

        saveButton.click();

        final var notification = _get(Notification.class);
        assertThat(notification.isOpened()).isTrue();

        final var notificationDiv = _get(notification, Div.class);
        assertThat(notificationDiv.getText()).startsWith("You do not have permission to edit the content of this page.");
    }

}
