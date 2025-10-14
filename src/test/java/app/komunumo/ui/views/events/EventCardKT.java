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
package app.komunumo.ui.views.events;

import app.komunumo.data.dto.EventDto;
import app.komunumo.data.dto.EventStatus;
import app.komunumo.data.dto.EventVisibility;
import app.komunumo.data.dto.EventWithImageDto;
import app.komunumo.ui.KaribuTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventCardKT extends KaribuTest {

    @Test
    @Tag("technical") // TODO just for coverage: remove after a page was added to display draft events without date/time
    void testEventWithoutDateTime() {
        final var event = new EventDto(null, null, null, null,
                "title", "description", "location",
                null, null, null,
                EventVisibility.PUBLIC, EventStatus.DRAFT);
        final var eventWithImage = new EventWithImageDto(event, null);

        final var eventCard = new EventCard(eventWithImage);
        assertThat(eventCard).isNotNull();
    }

}
