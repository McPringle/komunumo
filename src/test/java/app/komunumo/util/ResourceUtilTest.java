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
import org.mockito.MockedStatic;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ResourceUtilTest {

    @Test
    void getResourceAsString_success() {
        final var path = "/META-INF/resources/images/placeholder.svg";
        final var content = ResourceUtil.getResourceAsString(path, "fallback content");
        assertThat(content).startsWith("<?xml").doesNotContain("fallback content");
    }

    @Test
    void getResourceAsString_fallback() {
        final var path = "/META-INF/resources/images/non-existing.svg";
        final var content = ResourceUtil.getResourceAsString(path, "fallback content");
        assertThat(content).isEqualTo("fallback content");
    }

    @Test
    void getResourceAsString_withException() {
        final var inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("boom");
            }
        };

        try (MockedStatic<ResourceUtil> mocked = mockStatic(ResourceUtil.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> ResourceUtil.openResourceStream("/test.txt")).thenReturn(inputStream);

            final var output = ResourceUtil.getResourceAsString("/test.txt", "fallback");
            assertThat(output).isEqualTo("fallback");
        }
    }


}
