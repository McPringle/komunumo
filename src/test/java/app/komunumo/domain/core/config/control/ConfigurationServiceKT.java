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
package app.komunumo.domain.core.config.control;

import app.komunumo.domain.core.config.entity.ConfigurationSetting;
import app.komunumo.test.KaribuTest;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Locale;

import static app.komunumo.domain.core.config.entity.ConfigurationSetting.INSTANCE_NAME;
import static app.komunumo.domain.core.config.entity.ConfigurationSetting.INSTANCE_SLOGAN;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRENCH;
import static java.util.Locale.GERMAN;
import static java.util.Locale.ITALIAN;
import static java.util.Locale.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ConfigurationServiceKT extends KaribuTest {

    @Autowired
    private DSLContext dsl;

    private ConfigurationService configurationService;

    @BeforeEach
    void cleanSetUp() {
        configurationService = new ConfigurationService(dsl);
        configurationService.deleteAllConfigurations();
    }

    @Test
    void getConfiguration_languageDependentWithNullLocale_throwsIllegalArgumentException() {
        // Language-dependent setting requires a non-null locale
        assertThatThrownBy(() ->
                configurationService.getConfiguration(
                        INSTANCE_SLOGAN, null, String.class
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Setting 'instance.slogan' is language-dependent; you need to specify a locale!");
    }

    @Test
    void getConfiguration_languageIndependentWithLocale_throwsIllegalArgumentException() {
        // Language-independent setting must not receive a locale
        assertThatThrownBy(() ->
                configurationService.getConfiguration(
                        ConfigurationSetting.INSTANCE_NAME, ENGLISH, String.class
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Setting 'instance.name' is not language-dependent; do not specify a locale!");
    }

    @Test
    void shouldReturnDefaultValueIfNoValueExists() {
        final var value = configurationService.getConfiguration(INSTANCE_NAME);
        assertThat(value).isEqualTo("Your Instance Name");
    }

    @Test
    void shouldReturnValueFromDatabaseForLanguageSpecificSetting() {
        configurationService.setConfiguration(INSTANCE_SLOGAN, GERMAN, "Offener Community-Manager");
        final var value = configurationService.getConfiguration(INSTANCE_SLOGAN, GERMAN);
        assertThat(value).isEqualTo("Offener Community-Manager");
    }

    @Test
    void shouldFallbackToEnglishIfLanguageSpecificSettingIsMissing() {
        configurationService.setConfiguration(INSTANCE_SLOGAN, ENGLISH, "English Slogan");
        final var value = configurationService.getConfiguration(INSTANCE_SLOGAN, FRENCH);
        assertThat(value).isEqualTo("English Slogan");
    }

    @Test
    void shouldResolveLanguageFromRegionalLocale() {
        configurationService.setConfiguration(INSTANCE_SLOGAN, GERMAN, "Deutscher Slogan");

        final var swissGerman = of("de", "CH");
        final var value = configurationService.getConfiguration(INSTANCE_SLOGAN, swissGerman);

        assertThat(value).isEqualTo("Deutscher Slogan");
    }

    @Test
    void shouldReturnDefaultIfNothingIsSet() {
        final var value = configurationService.getConfiguration(INSTANCE_SLOGAN, ITALIAN);
        assertThat(value).isEqualTo("Your Instance Slogan");
    }

    @Test
    void shouldOverrideExistingValue() {
        configurationService.setConfiguration(INSTANCE_SLOGAN, ENGLISH, "Old");
        configurationService.setConfiguration(INSTANCE_SLOGAN, ENGLISH, "New");
        final var value = configurationService.getConfiguration(INSTANCE_SLOGAN, ENGLISH);
        assertThat(value).isEqualTo("New");
    }

    @Test
    void shouldInvalidateCacheWhenSettingIsChanged() {
        configurationService.setConfiguration(INSTANCE_SLOGAN, ENGLISH, "Initial");
        final var before = configurationService.getConfiguration(INSTANCE_SLOGAN, ENGLISH);
        assertThat(before).isEqualTo("Initial");

        configurationService.setConfiguration(INSTANCE_SLOGAN, ENGLISH, "Updated");
        final var after = configurationService.getConfiguration(INSTANCE_SLOGAN, ENGLISH);
        assertThat(after).isEqualTo("Updated");
    }

    @Test
    void shouldStoreAndRetrieveValueWithoutLocale() {
        configurationService.setConfiguration(INSTANCE_NAME, "Custom Komunumo");
        final var value = configurationService.getConfiguration(INSTANCE_NAME);
        assertThat(value).isEqualTo("Custom Komunumo");
    }

    @Test
    void shouldReturnValueWithoutLocaleWhenLocaleIsNull() {
        configurationService.setConfiguration(INSTANCE_NAME, "Neutral Name");
        final var value = configurationService.getConfiguration(INSTANCE_NAME, (Locale) null);
        assertThat(value).isEqualTo("Neutral Name");
    }

    @Test
    void shouldReturnDefaultAfterDeletingNonLanguageSetting() {
        configurationService.setConfiguration(INSTANCE_NAME, "Custom Komunumo");
        assertThat(configurationService.getConfiguration(INSTANCE_NAME)).isEqualTo("Custom Komunumo");

        configurationService.deleteConfiguration(INSTANCE_NAME);

        final var value = configurationService.getConfiguration(INSTANCE_NAME);
        assertThat(value).isEqualTo("Your Instance Name");
    }

    @Test
    void shouldDeleteLanguageSpecificValueOnlyForGivenLocale() {
        configurationService.setConfiguration(INSTANCE_SLOGAN, ENGLISH, "English Slogan");
        assertThat(configurationService.getConfiguration(INSTANCE_SLOGAN, ENGLISH))
                .isEqualTo("English Slogan");
        configurationService.setConfiguration(INSTANCE_SLOGAN, GERMAN, "Deutscher Slogan");
        assertThat(configurationService.getConfiguration(INSTANCE_SLOGAN, GERMAN))
                .isEqualTo("Deutscher Slogan");

        configurationService.deleteConfiguration(INSTANCE_SLOGAN, GERMAN);

        assertThat(configurationService.getConfiguration(INSTANCE_SLOGAN, GERMAN))
                .isEqualTo("English Slogan"); // Fallback
        assertThat(configurationService.getConfiguration(INSTANCE_SLOGAN, ENGLISH))
                .isEqualTo("English Slogan");
    }

    @Test
    void shouldFallbackToDefaultAfterDeletingLastLanguageValues() {
        configurationService.setConfiguration(INSTANCE_SLOGAN, ENGLISH, "English Slogan");
        assertThat(configurationService.getConfiguration(INSTANCE_SLOGAN, ENGLISH))
                .isEqualTo("English Slogan");

        configurationService.deleteConfiguration(INSTANCE_SLOGAN, ENGLISH);

        assertThat(configurationService.getConfiguration(INSTANCE_SLOGAN, ENGLISH))
                .isEqualTo("Your Instance Slogan");
    }

    @Test
    void shouldResolveRegionalLocaleOnDelete() {
        configurationService.setConfiguration(INSTANCE_SLOGAN, GERMAN, "Deutscher Slogan");
        assertThat(configurationService.getConfiguration(INSTANCE_SLOGAN, GERMAN))
                .isEqualTo("Deutscher Slogan");

        final var swissGerman = of("de", "CH");
        configurationService.deleteConfiguration(INSTANCE_SLOGAN, swissGerman);

        // The entry for "de" is considered deleted, therefore fallback to default
        assertThat(configurationService.getConfiguration(INSTANCE_SLOGAN, swissGerman))
                .isEqualTo("Your Instance Slogan");
    }

    @Test
    void deletingNonExistingConfigurationIsNoOp() {
        // Nothing set → Deletion must not fail
        configurationService.deleteConfiguration(INSTANCE_NAME);

        final var value = configurationService.getConfiguration(INSTANCE_NAME);
        assertThat(value).isEqualTo("Your Instance Name");
    }

    @Test
    void deletingWithNullLocaleForLanguageIndependentSettingDeletesNeutralValue() {
        configurationService.setConfiguration(INSTANCE_NAME, "Neutral");
        assertThat(configurationService.getConfiguration(INSTANCE_NAME)).isEqualTo("Neutral");

        configurationService.deleteConfiguration(INSTANCE_NAME, null);

        assertThat(configurationService.getConfiguration(INSTANCE_NAME)).isEqualTo("Your Instance Name");
    }

    static Object[][] getTestDataFor_testGettingConfigurationWithDifferentTypes() {
        return new Object[][]{
                {ConfigurationSetting.INSTANCE_URL, String.class, "https://example.com", "https://example.com"},
                {ConfigurationSetting.INSTANCE_HIDE_COMMUNITIES, Boolean.class, true, true},
                {ConfigurationSetting.INSTANCE_HIDE_COMMUNITIES, Boolean.class, false, false},
                {ConfigurationSetting.INSTANCE_CREATE_COMMUNITY_ALLOWED, Boolean.class, true, true},
                {ConfigurationSetting.INSTANCE_CREATE_COMMUNITY_ALLOWED, Boolean.class, false, false},
        };
    }

    @ParameterizedTest
    @MethodSource("getTestDataFor_testGettingConfigurationWithDifferentTypes")
    <T> void testGettingConfigurationWithDifferentTypes(final ConfigurationSetting setting,
                                                        final Class<T> type,
                                                        final T input,
                                                        final T expected) {
        configurationService.setConfiguration(setting, input);
        final var value = configurationService.getConfiguration(setting, type);
        assertThat(value).isEqualTo(expected);
    }

    @Test
    void testGettingConfigurationWithUnsupportedType() {
        assertThatThrownBy(() ->
                configurationService.getConfiguration(INSTANCE_NAME, List.class)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported type: interface java.util.List");
    }

}
