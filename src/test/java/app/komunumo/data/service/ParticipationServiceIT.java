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

import app.komunumo.ui.IntegrationTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ParticipationServiceIT extends IntegrationTest {

    @Autowired
    private @NotNull ParticipationService participationService;

    @Autowired
    private @NotNull EventService eventService;

    @Test
    void requestVerificationCode() {
        final var event = eventService.getUpcomingEventsWithImage().getFirst().event();
        assertThat(participationService.requestVerificationCode(null, "")).isFalse();
        assertThat(participationService.requestVerificationCode(event.id(), "")).isFalse();
        assertThat(participationService.requestVerificationCode(event.id(), "foobar@komunumo.test")).isTrue();
    }

}
