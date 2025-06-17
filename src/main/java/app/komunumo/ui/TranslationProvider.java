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
package app.komunumo.ui;

import com.vaadin.flow.i18n.I18NProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


@Component
public final class TranslationProvider implements I18NProvider {

    public static final @NotNull String BUNDLE_PREFIX = "i18n.messages";
    private static final @NotNull List<Locale> SUPPORTED_LOCALES = List.of(Locale.ENGLISH, Locale.GERMAN);
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(TranslationProvider.class);

    @Override
    public @NotNull List<Locale> getProvidedLocales() {
        return SUPPORTED_LOCALES;
    }

    @Override
    public @NotNull String getTranslation(final @NotNull String key,
                                          final @Nullable Locale locale,
                                          final @NotNull Object... params) {
        if (locale == null) {
            LOGGER.info("No locale set for key '{}', fallback to english", key);
        }
        final var effectiveLocale = locale != null ? locale : Locale.ENGLISH;
        try {
            final var bundle = ResourceBundle.getBundle(BUNDLE_PREFIX, effectiveLocale);
            final var value = bundle.getString(key);
            final var translation = params.length > 0 ? MessageFormat.format(value, params) : value;
            LOGGER.info("Translation for key '{}' in locale '{}' is: {}", key, effectiveLocale, translation);
            return  translation;
        } catch (final MissingResourceException e) {
            LOGGER.warn("Missing translation for key: {} in locale: {}", key, effectiveLocale, e);
            return "!!" + key + "!!";
        }
    }

}
