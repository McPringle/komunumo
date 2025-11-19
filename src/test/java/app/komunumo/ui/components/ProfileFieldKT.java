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
package app.komunumo.ui.components;

import app.komunumo.business.core.config.entity.ConfigurationSetting;
import app.komunumo.business.core.config.control.ConfigurationService;
import app.komunumo.test.KaribuTest;
import app.komunumo.ui.components.ProfileField.ProfileNameAvailabilityChecker;
import com.vaadin.flow.component.html.Paragraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfileFieldKT extends KaribuTest {

    private ProfileField profileField;

    @BeforeEach
    void setUp() {
        final var configurationService = mock(ConfigurationService.class);
        when(configurationService.getConfiguration(ConfigurationSetting.INSTANCE_DOMAIN)).thenReturn("example.com");
        final ProfileNameAvailabilityChecker availabilityChecker = profileName -> !profileName.contains("taken");
        profileField = new ProfileField(configurationService, availabilityChecker);
    }

    @Test
    void profileNameIsAvailable() {
        profileField.setValue("@available@example.com");
        final var message = _get(profileField, Paragraph.class);
        assertThat(message.getText()).isEqualTo("This profile name is available.");
    }

    @Test
    void profileNameIsNotAvailable() {
        profileField.setValue("@taken@example.com");
        final var message = _get(profileField, Paragraph.class);
        assertThat(message.getText()).isEqualTo("This profile name is not available!");
    }

    @Test
    void profileNameIsTooShort() {
        profileField.setValue("@ab@example.com");
        final var message = _get(profileField, Paragraph.class);
        assertThat(message.getText()).isEqualTo("The profile name must be between 3 and 30 characters long!");
    }

    @Test
    void profileNameIsTooLong() {
        profileField.setValue("@abcdefghijklmnopqrstuvwxyz1234567890@example.com");
        final var message = _get(profileField, Paragraph.class);
        assertThat(message.getText()).isEqualTo("The profile name must be between 3 and 30 characters long!");
    }

    @Test
    void profileNameInvalidSyntax() {
        profileField.setValue("test@example.com");
        final var message = _get(profileField, Paragraph.class);
        assertThat(message.getText()).isEqualTo("The syntax of the profile name is invalid: test@example.com");
    }

    @Test
    void profileNameInvalidDomain() {
        profileField.setValue("@test@foo.bar");
        final var message = _get(profileField, Paragraph.class);
        assertThat(message.getText()).isEqualTo("The domain name \"foo.bar\" is not the domain name of this instance (\"example.com\")!");
    }

    @Test
    void setReadOnly() {
        assertThat(profileField.isReadOnly()).isFalse();
        profileField.setReadOnly(true);
        assertThat(profileField.isReadOnly()).isTrue();
        profileField.setReadOnly(false);
        assertThat(profileField.isReadOnly()).isFalse();
    }

    @Test
    void setRequired() {
        assertThat(profileField.isRequired()).isFalse();
        profileField.setRequired(true);
        assertThat(profileField.isRequired()).isTrue();
        profileField.setRequired(false);
        assertThat(profileField.isRequired()).isFalse();
    }

    @Test
    void setRequiredIndicatorVisible() {
        assertThat(profileField.isRequiredIndicatorVisible()).isFalse();
        profileField.setRequiredIndicatorVisible(true);
        assertThat(profileField.isRequiredIndicatorVisible()).isTrue();
        profileField.setRequiredIndicatorVisible(false);
        assertThat(profileField.isRequiredIndicatorVisible()).isFalse();
    }

    @Test
    void returnOriginalProfileName() {
        profileField.setValue("@test@foo.bar");
        assertThat(profileField.getValue()).isEqualTo("@test@foo.bar");
    }

}
