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
package org.komunumo.data.service;

import org.jetbrains.annotations.NotNull;
import org.komunumo.data.db.tables.records.EventRecord;
import org.komunumo.data.entity.Event;
import org.komunumo.data.service.getter.DSLContextGetter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.komunumo.data.db.tables.Event.EVENT;

interface EventService extends DSLContextGetter {

    @NotNull
    default Stream<Event> upcomingEvents() {
        return dsl().selectFrom(EVENT)
                .where(EVENT.DATE.greaterOrEqual(LocalDateTime.now().withHour(0).withMinute(0)))
                .orderBy(EVENT.DATE.asc())
                .fetch()
                .stream()
                .map(this::mapEventRecord)
                .filter(event -> event.duration() != null)
                .filter(event -> {
                    final var now = LocalDateTime.now();
                    final var endDate = event.date().plus(event.duration());
                    return endDate.isAfter(now);
                });
    }

    @NotNull
    private Event mapEventRecord(@NotNull final EventRecord eventRecord) {
        final var duration = eventRecord.getDuration() == null ? null
                : Duration.ofHours(eventRecord.getDuration().getHour())
                        .plusMinutes(eventRecord.getDuration().getMinute());
        return new Event(
                eventRecord.getId(),
                eventRecord.getTitle(),
                eventRecord.getSubtitle(),
                eventRecord.getDescription(),
                eventRecord.getDate(),
                duration);
    }

}
