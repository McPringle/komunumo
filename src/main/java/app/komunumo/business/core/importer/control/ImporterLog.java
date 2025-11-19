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
package app.komunumo.business.core.importer.control;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ImporterLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImporterLog.class);

    private final @Nullable LogCallback callback;

    public ImporterLog(final @Nullable LogCallback callback) {
        super();
        this.callback = callback;
    }

    public void info(final @NotNull String message) {
        LOGGER.info(message);
        if (callback != null) {
            callback.log("ℹ️ " + message);
        }
    }

    public void warn(final @NotNull String message) {
        LOGGER.warn(message);
        if (callback != null) {
            callback.log("⚠️ " + message);
        }
    }

    public void error(final @NotNull String message) {
        LOGGER.error(message);
        if (callback != null) {
            callback.log("⛔ " + message);
        }
    }

}
