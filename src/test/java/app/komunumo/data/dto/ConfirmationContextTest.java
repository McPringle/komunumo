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
package app.komunumo.data.dto;

import app.komunumo.data.service.ConfirmationContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfirmationContextTest {

    @Test
    void ofKeyValuePairs() {
        final var confirmationContext = ConfirmationContext.of(
                "key1", "value1",
                "key2", "value2",
                "key3", "value3");

        assertThat(confirmationContext).isNotNull().hasSize(3);
        assertThat(confirmationContext.get("key1")).isEqualTo("value1");
        assertThat(confirmationContext.get("key2")).isEqualTo("value2");
        assertThat(confirmationContext.get("key3")).isEqualTo("value3");
    }

    @Test
    void ofEntries() {
        final var confirmationContext = ConfirmationContext.of(
                Map.entry("key1", "value1"),
                Map.entry("key2", "value2"),
                Map.entry("key3", "value3"));

        assertThat(confirmationContext).isNotNull().hasSize(3);
        assertThat(confirmationContext.get("key1")).isEqualTo("value1");
        assertThat(confirmationContext.get("key2")).isEqualTo("value2");
        assertThat(confirmationContext.get("key3")).isEqualTo("value3");
    }

    @Test
    void keyAndValuesMustBeInPairs() {
        assertThatThrownBy(() -> {
            ConfirmationContext.of(
                    "key1", "value1",
                    "key2", "value2",
                    "key3"); // missing value for key3
        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Keys and values must be in pairs");
    }

    @Test
    void getStringExistingKey() {
        final var confirmationContext = ConfirmationContext.of("key1", "value1");
        assertThat(confirmationContext.getString("key1")).isEqualTo("value1");
    }

    @Test
    void getStringNonExistingKey() {
        final var confirmationContext = ConfirmationContext.empty();
        assertThatThrownBy(() -> {
            confirmationContext.getString("nonExistingKey");
        })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Key 'nonExistingKey' does not exist");
    }

    @Test
    void getStringNullValue() {
        @SuppressWarnings("DataFlowIssue")
        final var confirmationContext = ConfirmationContext.of("key1", null);
        assertThatThrownBy(() -> {
            confirmationContext.getString("key1");
        })
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Key 'key1' is null");
    }

}
