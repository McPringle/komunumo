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

            assertEquals("/events/abcdef", result);
        }

        @Test
        @DisplayName("Root path")
        void root_path() {
            final var location = new Location("");
            final var ui = mockUiFor(location);

            final var result = LocationUtil.getCurrentLocation(ui);

            assertEquals("/", result);
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

            assertEquals("/events/abcdef?foo=bar", result);
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

            assertEquals("/events/abcdef?a=1&b=2", result);
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
            assertEquals("/events/abcdef?x=1&x=2", result);
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

            assertEquals("/search/results?q=hello%20world&q=gr%C3%BC%C3%9Fe&q=a%3Db", result);
        }

        @Test
        @DisplayName("Empty parameter map behaves like no parameters")
        void empty_params_map() {
            final var queryParameters = new QueryParameters(Map.of());
            final var location = new Location("events/abcdef", queryParameters);
            final var ui = mockUiFor(location);

            final var result = LocationUtil.getCurrentLocation(ui);

            assertEquals("/events/abcdef", result);
        }
    }
}
