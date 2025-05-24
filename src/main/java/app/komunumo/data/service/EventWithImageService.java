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

import app.komunumo.data.dto.EventDto;
import app.komunumo.data.dto.EventWithImageDto;
import app.komunumo.data.dto.ImageDto;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static app.komunumo.data.db.tables.Event.EVENT;
import static app.komunumo.data.db.tables.Image.IMAGE;

@Service
public final class EventWithImageService {

    private final @NotNull DSLContext dsl;

    public EventWithImageService(final @NotNull DSLContext dsl) {
        this.dsl = dsl;
    }

    public @NotNull List<@NotNull EventWithImageDto> getUpcomingEventsWithImages() {
        return dsl.select()
                .from(EVENT)
                .leftJoin(IMAGE).on(EVENT.IMAGE_ID.eq(IMAGE.ID))
                .where(EVENT.END.isNotNull().and(EVENT.END.gt(ZonedDateTime.now(ZoneOffset.UTC))))
                .fetch(rec -> new EventWithImageDto(
                        rec.into(EVENT).into(EventDto.class),
                        rec.get(IMAGE.ID) != null ? rec.into(IMAGE).into(ImageDto.class) : null
                ));
    }

}
