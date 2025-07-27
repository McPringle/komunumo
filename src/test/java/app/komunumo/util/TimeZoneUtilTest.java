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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.server.VaadinSession;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TimeZoneUtilTest {

    private VaadinSession mockSession;

    @BeforeEach
    void setup() {
        mockSession = mock(VaadinSession.class);
        VaadinSession.setCurrent(mockSession);
    }

    @Test
    void returnsClientTimeZoneIfSet() {
        final var clientZone = ZoneId.of("Europe/Zurich");
        when(mockSession.getAttribute("CLIENT_TIMEZONE_ID")).thenReturn(clientZone);
        final var result = TimeZoneUtil.getClientTimeZone();
        assertThat(result).isEqualTo(clientZone);
    }

    @Test
    void returnsSystemDefaultTimeZoneIfNotSet() {
        when(mockSession.getAttribute("CLIENT_TIMEZONE_ID")).thenReturn(null);
        final var result = TimeZoneUtil.getClientTimeZone();
        assertThat(result).isEqualTo(ZoneId.systemDefault());
    }

    @ParameterizedTest
    @ValueSource(strings = { "Europe/Zurich", "UTC", "America/New_York" })
    @SuppressWarnings("java:S6068") // false positive for mock verification
    void detectClientTimeZoneStoresZoneIdInSession(final @NotNull String zoneIdString) {
        final var mockUI = mock(UI.class);
        final var mockPage = mock(Page.class);
        when(mockUI.getPage()).thenReturn(mockPage);
        ArgumentCaptor<Page.ExtendedClientDetailsReceiver> captor =
                ArgumentCaptor.forClass(Page.ExtendedClientDetailsReceiver.class);

        TimeZoneUtil.detectClientTimeZone(mockUI);
        verify(mockPage).retrieveExtendedClientDetails(captor.capture());

        final var mockDetails = mock(ExtendedClientDetails.class);
        when(mockDetails.getTimeZoneId()).thenReturn(zoneIdString);
        captor.getValue().receiveDetails(mockDetails);

        verify(mockSession).setAttribute(eq("CLIENT_TIMEZONE_ID"), eq(ZoneId.of(zoneIdString)));
    }

}
