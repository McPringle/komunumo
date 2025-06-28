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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateUtilTest {

    @Test
    void replaceVariablesWithMappingStrings() {
        final var text = "Hello, ${name}!";
        final var variables = Map.of("name", "World");
        final var result = TemplateUtil.replaceVariables(text, variables);
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    void replaceVariablesWithNonMappingStrings() {
        final var text = "Hello, ${name}!";
        final var variables = Map.of("foobar", "World");
        final var result = TemplateUtil.replaceVariables(text, variables);
        assertThat(result).isEqualTo("Hello, ${name}!");
    }

    @Test
    void replaceVariablesWithEmptyMap() {
        final var text = "Hello, ${name}!";
        final Map<String, String> variables = Map.of();
        final var result = TemplateUtil.replaceVariables(text, variables);
        assertThat(result).isEqualTo("Hello, ${name}!");
    }

    @Test
    void replaceVariablesWithNullMap() {
        final var text = "Hello, ${name}!";
        final var result = TemplateUtil.replaceVariables(text, null);
        assertThat(result).isEqualTo("Hello, ${name}!");
    }

}
