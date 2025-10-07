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
package app.komunumo.ui.views.admin.config;

import app.komunumo.data.dto.ConfigurationSetting;
import app.komunumo.data.service.ConfigurationService;
import app.komunumo.ui.TranslationProvider;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.jetbrains.annotations.NotNull;

class ConfigurationEditorComponent extends VerticalLayout {

    ConfigurationEditorComponent(final @NotNull ConfigurationService configurationService,
                                 final @NotNull ConfigurationSetting configurationSetting) {
        super();

        final var defaultValue = configurationSetting.defaultValue();
        final var label = getTranslation("ui.views.admin.config.ConfigurationEditorView.label." + configurationSetting.setting());

        if (configurationSetting.isLanguageDependent()) {
            for (final var locale : TranslationProvider.getSupportedLocales()) {
                final var localizedValue = configurationService.getConfiguration(configurationSetting, locale);
                final var localizedLabel = label + " (" + locale.getDisplayLanguage(locale) + ")";
                final var textField = new TextField(localizedLabel);
                textField.setPlaceholder(defaultValue);
                if (!defaultValue.equals(localizedValue)) {
                    textField.setValue(localizedValue);
                }
                add(textField);
            }
        } else {
            final var actualValue = configurationService.getConfiguration(configurationSetting);

            final var textField = new TextField();
            textField.setLabel(label);
            textField.setPlaceholder(defaultValue);

            if (!defaultValue.equals(actualValue)) {
                textField.setValue(actualValue);
            }

            add(textField);
        }
    }

}
