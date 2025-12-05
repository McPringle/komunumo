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
package app.komunumo.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ResourceUtil {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(ResourceUtil.class);

    public static @NotNull String getResourceAsString(final @NotNull String path,
                                                      final @NotNull String fallback) {
        String returnValue = fallback;
        try (InputStream inputStream = openResourceStream(path)) {
            if (inputStream != null) {
                returnValue = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } else  {
                LOGGER.warn("Resource not found: {}", path);
            }
        } catch (final IOException e) {
            LOGGER.warn(e.getMessage());
        }
        return returnValue;
    }

    @VisibleForTesting
    public static @Nullable InputStream openResourceStream(final @NotNull String path) {
        return ResourceUtil.class.getResourceAsStream(path);
    }

    private ResourceUtil() {
        throw new IllegalStateException("Utility class");
    }

}
