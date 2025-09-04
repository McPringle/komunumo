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

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class ConfirmationContext extends HashMap<String, Object> {

    public static @NotNull ConfirmationContext of(final @NotNull Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Keys and values must be in pairs");
        }

        final var confirmationContext = new ConfirmationContext();

        for (int i = 0; i < keyValues.length; i += 2) {
            final var key = (String) keyValues[i];
            final var value = keyValues[i + 1];
            confirmationContext.put(key, value);
        }

        return confirmationContext;
    }

    @SafeVarargs
    public static @NotNull ConfirmationContext of(final @NotNull Map.Entry<String, ?>... entries) {
        final var confirmationContext = new ConfirmationContext();

        for (final var entry : entries) {
            confirmationContext.put(entry.getKey(), entry.getValue());
        }

        return confirmationContext;
    }

    public static @NotNull ConfirmationContext empty() {
        return new ConfirmationContext();
    }

    public @NotNull String getString(final @NotNull String contextKey) {
        if (!containsKey(contextKey)) {
            throw new IllegalArgumentException(String.format("Key '%s' does not exist", contextKey));
        }
        if (get(contextKey) == null) {
            throw new NullPointerException(String.format("Key '%s' is null", contextKey));
        }
        return (String) get(contextKey);
    }

}
