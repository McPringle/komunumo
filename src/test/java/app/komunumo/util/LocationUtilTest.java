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
import com.vaadin.flow.component.internal.UIInternals;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationUtilTest {

    private UI mockUiFor(final @NotNull Location location) {
        final var ui = mock(UI.class);
        final var internals = mock(UIInternals.class);

        when(ui.getInternals()).thenReturn(internals);
        when(internals.getActiveViewLocation()).thenReturn(location);

        return ui;
    }

    @Nested
    @DisplayName("Path without query parameters")
    class PathOnly {

        @Test
        @DisplayName("Normal path")
        void path_only() {
            final var location = new Location("events/abcdef");
            final var ui = mockUiFor(location);

            final var result = LocationUtil.getCurrentLocation(ui);

            assertThat(result).isEqualTo("/events/abcdef");
        }

        @Test
        @DisplayName("Root path")
        void root_path() {
            final var location = new Location("");
            final var ui = mockUiFor(location);

            final var result = LocationUtil.getCurrentLocation(ui);

            assertThat(result).isEqualTo("/");
        }
    }

    @Nested
    @DisplayName("Path with query parameters")
    class PathWithQuery {

        @Test
        @DisplayName("Single parameter")
        void single_param() {
            final var queryParameters = QueryParameters.simple(Map.of("foo", "bar"));
            final var location = new Location("events/abcdef", queryParameters);
            final var ui = mockUiFor(location);

            final var result = LocationUtil.getCurrentLocation(ui);

            assertThat(result).isEqualTo("/events/abcdef?foo=bar");
        }

        @Test
        @DisplayName("Multiple parameters in insertion order")
        void multiple_params_order_preserved() {
            // LinkedHashMap ensures insertion order is preserved
            final Map<String, List<String>> params = new LinkedHashMap<>();
            params.put("a", List.of("1"));
            params.put("b", List.of("2"));
            final var queryParameters = new QueryParameters(params);

            final var location = new Location("events/abcdef", queryParameters);
            final var ui = mockUiFor(location);

            final var result = LocationUtil.getCurrentLocation(ui);

            assertThat(result).isEqualTo("/events/abcdef?a=1&b=2");
        }

        @Test
        @DisplayName("Multiple values for the same key")
        void multi_values_same_key() {
            final Map<String, List<String>> params = new LinkedHashMap<>();
            params.put("x", List.of("1", "2"));
            final var queryParameters = new QueryParameters(params);

            final var location = new Location("events/abcdef", queryParameters);
            final var ui = mockUiFor(location);

            final var result = LocationUtil.getCurrentLocation(ui);

            // Order of values is preserved
            assertThat(result).isEqualTo("/events/abcdef?x=1&x=2");
        }

        @Test
        @DisplayName("URL encoding (spaces, umlauts, special characters)")
        void url_encoding() {
            final Map<String, List<String>> params = new LinkedHashMap<>();
            // Space -> '+'; 'ü' -> %C3%BC; '=' in value -> %3D
            params.put("q", List.of("hello world", "grüße", "a=b"));
            final var queryParameters = new QueryParameters(params);

            final var location = new Location("search/results", queryParameters);
            final var ui = mockUiFor(location);

            final var result = LocationUtil.getCurrentLocation(ui);

            assertThat(result).isEqualTo("/search/results?q=hello%20world&q=gr%C3%BC%C3%9Fe&q=a%3Db");
        }

        @Test
        @DisplayName("Empty parameter map behaves like no parameters")
        void empty_params_map() {
            final var queryParameters = new QueryParameters(Map.of());
            final var location = new Location("events/abcdef", queryParameters);
            final var ui = mockUiFor(location);

            final var result = LocationUtil.getCurrentLocation(ui);

            assertThat(result).isEqualTo("/events/abcdef");
        }
    }
}
