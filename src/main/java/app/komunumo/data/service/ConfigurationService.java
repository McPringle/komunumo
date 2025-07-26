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
package app.komunumo.data.service;

import app.komunumo.data.dto.ConfigurationSetting;
import app.komunumo.util.LocaleUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

import static app.komunumo.data.db.tables.Config.CONFIG;

@Service
public final class ConfigurationService {

    private final DSLContext dsl;
    private final Cache<CacheKey, String> cache;

    public ConfigurationService(final @NotNull DSLContext dsl) {
        this.dsl = dsl;
        this.cache = Caffeine.newBuilder()
                .maximumSize(1_000)
                .build();
    }

    public @NotNull String getConfiguration(final @NotNull ConfigurationSetting setting) {
        return getConfiguration(setting, null);
    }

    public @NotNull String getConfiguration(final @NotNull ConfigurationSetting setting,
                                            final @Nullable Locale locale) {
        if (locale == null) {
            return getFromCacheOrDb(setting, null).orElse(setting.defaultValue());
        }

        final var languageCode = LocaleUtil.getLanguageCode(locale);
        return getFromCacheOrDb(setting, languageCode)
                .or(() -> getFromCacheOrDb(setting, "EN"))
                .orElse(setting.defaultValue());
    }

    private @NotNull Optional<String> getFromCacheOrDb(final @NotNull ConfigurationSetting setting,
                                                       final @Nullable String language) {
        return Optional.ofNullable(cache.get(new CacheKey(setting, language), cacheKey ->
                dsl.select(CONFIG.VALUE)
                        .from(CONFIG)
                        .where(CONFIG.SETTING.eq(cacheKey.setting.setting()))
                        .and(language == null ? CONFIG.LANGUAGE.isNull() : CONFIG.LANGUAGE.eq(language))
                        .fetchOptional(CONFIG.VALUE)
                        .orElse(null)
        ));
    }

    public void setConfiguration(final @NotNull ConfigurationSetting setting,
                                 final @NotNull String value) {
        setConfiguration(setting, null, value);
    }

    public void setConfiguration(final @NotNull ConfigurationSetting setting,
                                 final @Nullable Locale locale,
                                 final @NotNull String value) {
        final var languageCode = LocaleUtil.getLanguageCode(locale);
        dsl.insertInto(CONFIG)
                .set(CONFIG.SETTING, setting.setting())
                .set(CONFIG.LANGUAGE, languageCode)
                .set(CONFIG.VALUE, value)
                .onDuplicateKeyUpdate()
                .set(CONFIG.VALUE, value)
                .execute();

        cache.asMap().keySet().removeIf(cacheKey -> cacheKey.setting().equals(setting));
    }

    public void clearCache() {
        cache.invalidateAll();
    }

    public void deleteAllSettings() {
        dsl.delete(CONFIG).execute();
        clearCache();
    }

    private record CacheKey(@NotNull ConfigurationSetting setting, @Nullable String language) { }

}
