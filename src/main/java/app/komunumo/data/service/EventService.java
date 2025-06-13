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

import app.komunumo.data.db.tables.records.EventRecord;
import app.komunumo.data.dto.EventDto;
import app.komunumo.data.generator.UniqueIdGenerator;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static app.komunumo.data.db.tables.Event.EVENT;

@Service
public final class EventService {

    private final @NotNull DSLContext dsl;
    private final @NotNull UniqueIdGenerator idGenerator;

    public EventService(final @NotNull DSLContext dsl,
                        final @NotNull UniqueIdGenerator idGenerator) {
        super();
        this.dsl = dsl;
        this.idGenerator = idGenerator;
    }

    public @NotNull EventDto storeEvent(final @NotNull EventDto event) {
        final EventRecord eventRecord = dsl.fetchOptional(EVENT, EVENT.ID.eq(event.id()))
                .orElse(dsl.newRecord(EVENT));
        eventRecord.from(event);

        if (eventRecord.getId() == null) { // NOSONAR (false positive: ID may be null for new events)
            eventRecord.setId(idGenerator.getUniqueID(EVENT));
        }

        final var now = ZonedDateTime.now(ZoneOffset.UTC);
        if (eventRecord.getCreated() == null) { // NOSONAR (false positive: date may be null for new events)
            eventRecord.setCreated(now);
            eventRecord.setUpdated(now);
        } else {
            eventRecord.setUpdated(now);
        }
        eventRecord.store();
        return eventRecord.into(EventDto.class);
    }

    public @NotNull Optional<@NotNull EventDto> getEvent(final @NotNull UUID id) {
        return dsl.selectFrom(EVENT)
                .where(EVENT.ID.eq(id))
                .fetchOptionalInto(EventDto.class);
    }

    public @NotNull List<@NotNull EventDto> getEvents() {
        return dsl.selectFrom(EVENT)
                .fetchInto(EventDto.class);
    }

    public int getEventCount() {
        return Optional.ofNullable(
                dsl.selectCount()
                        .from(EVENT)
                        .fetchOne(0, Integer.class)
        ).orElse(0);
    }

    public boolean deleteEvent(final @NotNull EventDto event) {
        return dsl.delete(EVENT)
                .where(EVENT.ID.eq(event.id()))
                .execute() > 0;
    }

}
