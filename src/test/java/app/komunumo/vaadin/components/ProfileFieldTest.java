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
package app.komunumo.vaadin.components;

import app.komunumo.domain.core.config.control.ConfigurationService;
import app.komunumo.domain.core.config.entity.ConfigurationSetting;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfileFieldTest {

    private ProfileField profileField;

    @BeforeEach
    void setUp() {
        final var configurationService = mock(ConfigurationService.class);
        final var profileNameAvailabilityChecker = mock (ProfileField.ProfileNameAvailabilityChecker.class);
        when(configurationService.getConfiguration(ConfigurationSetting.INSTANCE_DOMAIN)).thenReturn("example.com");
        profileField = new ProfileField(configurationService, profileNameAvailabilityChecker);
    }

    @Test
    void setLabel() {
        assertThat(profileField.getLabel()).isNull();
        final var label = "Test Label";
        profileField.setLabel(label);
        assertThat(profileField.getLabel()).isEqualTo(label);
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
    void setValueChangeMode() {
        assertThat(profileField.getValueChangeMode()).isEqualTo(ValueChangeMode.EAGER);
        profileField.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        assertThat(profileField.getValueChangeMode()).isEqualTo(ValueChangeMode.ON_CHANGE);
        profileField.setValueChangeMode(ValueChangeMode.ON_BLUR);
        assertThat(profileField.getValueChangeMode()).isEqualTo(ValueChangeMode.ON_BLUR);
    }

    @Test
    void getEmptyValue() {
        assertThat(profileField.getEmptyValue()).isEqualTo("");
    }

    @Test
    void generateModelValue() {
        assertThat(profileField.generateModelValue()).isEqualTo("");
        profileField.setValue("@testValue@example.com");
        assertThat(profileField.generateModelValue()).isEqualTo("testValue");
        assertThat(profileField.getValue()).isEqualTo("@testValue@example.com");
    }
}
