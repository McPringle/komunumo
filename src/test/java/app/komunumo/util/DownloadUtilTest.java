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

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadUtilTest {

    private static HttpServer server;

    @BeforeAll
    static void startServer() throws IOException {
        final var cssFile = Path.of("src/test/resources/custom-styles/styles.css");

        final HttpHandler handler = exchange -> {
            final var path = exchange.getRequestURI().getPath();
            if (path.equals("/styles.css")) {
                final byte[] bytes = Files.readAllBytes(cssFile);
                exchange.getResponseHeaders().set("Content-Type", "text/css");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else if (path.equals("/99")) {
                exchange.sendResponseHeaders(99, -1);
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        };

        server = HttpServer.create(new InetSocketAddress(8082), 0);
        server.createContext("/", handler);
        server.start();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @Test
    void getString() throws Exception {
        final String string = DownloadUtil.getString("http://localhost:8082/styles.css").trim();
        assertThat(string).startsWith(":root {").endsWith("}");
    }

    @Test
    void downloadFileSuccess() {
        final var path = DownloadUtil.downloadFile("http://localhost:8082/styles.css");
        assertThat(path).isNotNull().exists();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:8082/99",
            "http://localhost:8082/404",
            "http://localhost:8888/"
    })
    void shouldReturnNullForInvalidOrUnreachableUrls(final String url) {
        final var path = DownloadUtil.downloadFile(url);
        assertThat(path).isNull();
    }

}
