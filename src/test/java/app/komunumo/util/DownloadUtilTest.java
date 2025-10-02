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

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadUtilTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void getString() throws Exception {
        String css = "body::after { content: 'test'; }";
        server.enqueue(new MockResponse().setBody(css).setResponseCode(200));

        String url = server.url("/styles.css").toString();
        String string = DownloadUtil.getString(url).trim();

        assertThat(string).isEqualTo(css);
    }

    @Test
    void downloadFileSuccess() throws Exception {
        String content = "hello world";
        server.enqueue(new MockResponse().setBody(content).setResponseCode(200));

        String url = server.url("/file.txt").toString();
        var path = DownloadUtil.downloadFile(url);

        assertThat(path).isNotNull().exists();
        assertThat(Files.readString(path, StandardCharsets.UTF_8)).isEqualTo(content);
    }

    @Test
    void shouldReturnNullForInvalidStatusCode() {
        server.enqueue(new MockResponse().setResponseCode(404));

        String url = server.url("/not-found").toString();
        var path = DownloadUtil.downloadFile(url);

        assertThat(path).isNull();
    }
}
