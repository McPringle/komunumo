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
package app.komunumo.domain.core.config.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>Represents a stored configuration value from the database.</p>
 *
 * <p>This record is used for exporting configuration data and contains
 * the raw database values including the setting key, language code, and value.</p>
 *
 * @param setting the unique key of the configuration setting
 * @param language the ISO language code (e.g., "EN") or empty string for language-independent values
 * @param value the stored configuration value
 */
public record ConfigurationValue(
        @NotNull String setting,
        @Nullable String language,
        @NotNull String value
) { }
