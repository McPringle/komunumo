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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadUtilTest {

    @Test
    void getString() throws Exception {
        final String string = DownloadUtil.getString("http://localhost:8082/custom-styles/styles.css").trim();
        assertThat(string).startsWith(":root {").endsWith("}");
    }

    @Test
    void downloadFileSuccess() {
        final var path = DownloadUtil.downloadFile("http://localhost:8082/custom-styles/styles.css");
        assertThat(path).isNotNull().exists();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:8082/99",
            "http://localhost:8082/non-existing",
            "http://localhost:8888/"
    })
    void shouldReturnNullForInvalidOrUnreachableUrls(final String url) {
        final var path = DownloadUtil.downloadFile(url);
        assertThat(path).isNull();
    }

}
