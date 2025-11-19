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
package app.komunumo.business.core.i18n.controller;

import com.vaadin.flow.i18n.DefaultI18NProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;


@Component
public final class TranslationProvider extends DefaultI18NProvider {

    private static final @NotNull List<Locale> SUPPORTED_LOCALES = List.of(
            Locale.ENGLISH, Locale.GERMAN);

    public static @NotNull List<Locale> getSupportedLocales() {
        return SUPPORTED_LOCALES;
    }

    public TranslationProvider() {
        super(getSupportedLocales());
        Locale.setDefault(Locale.ENGLISH);
    }

    @Override
    public @NotNull String getTranslation(final @NotNull String key,
                                          final @Nullable Locale locale,
                                          final @NotNull Object... params) {
        final var effectiveLocale = locale != null ? locale : Locale.ENGLISH;
        return super.getTranslation(key, effectiveLocale, params);
    }

}
