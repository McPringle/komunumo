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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EventWithImageServiceTest {

    @Autowired
    private @NotNull EventWithImageService eventWithImageService;

    @Test
    void getUpcomingEventsWithImages() {
        final var now = ZonedDateTime.now(ZoneOffset.UTC);
        final var upcomingEvents = eventWithImageService.getUpcomingEventsWithImages();
        assertThat(upcomingEvents).hasSize(3);
        assertThat(upcomingEvents)
                .extracting(EventWithImageDto::event)
                .extracting(EventDto::title)
                .containsExactly("Demo Event 3", "Demo Event 5", "Demo Event 6");
        assertThat(upcomingEvents)
                .extracting(EventWithImageDto::event)
                .extracting(EventDto::end)
                .allSatisfy(endDate -> assertThat(endDate).isAfter(now));
    }

}
