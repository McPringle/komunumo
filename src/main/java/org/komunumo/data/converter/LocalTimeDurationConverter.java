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
package org.komunumo.data.converter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Converter;

import java.time.Duration;
import java.time.LocalTime;

public final class LocalTimeDurationConverter implements Converter<LocalTime, Duration> {
    @Override
    public Duration from(@Nullable final LocalTime databaseObject) {
        if (databaseObject == null) {
            return null;
        }
        return Duration.between(LocalTime.of(0, 0), databaseObject);
    }

    @Override
    public LocalTime to(@Nullable final Duration userObject) {
        if (userObject == null) {
            return null;
        }
        return LocalTime.of(0, 0).plus(userObject);
    }

    @Override
    public @NotNull Class<LocalTime> fromType() {
        return LocalTime.class;
    }

    @Override
    public @NotNull Class<Duration> toType() {
        return Duration.class;
    }
}
