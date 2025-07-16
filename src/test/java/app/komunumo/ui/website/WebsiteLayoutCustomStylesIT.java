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
package app.komunumo.ui.website;

import app.komunumo.ui.BrowserTest;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class WebsiteLayoutCustomStylesIT extends BrowserTest {

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static HttpServer server;

    @Override
    protected String[] getProperties() {
        return new String[] {
                "--komunumo.custom.styles=http://localhost:8082/styles.css"
        };
    }

    @BeforeAll
    static void startServer() throws IOException {
        final var cssFile = Path.of("src/test/resources/custom-styles/styles.css");

        final HttpHandler handler = exchange -> {
            if (exchange.getRequestURI().getPath().equals("/styles.css")) {
                counter.incrementAndGet();
                final byte[] bytes = Files.readAllBytes(cssFile);
                exchange.getResponseHeaders().set("Content-Type", "text/css");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
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

    private void testWebserver() throws URISyntaxException, IOException, InterruptedException {
        try (var client = HttpClient.newHttpClient()) {
            var response = client.send(
                    java.net.http.HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8082/styles.css"))
                            .build(),
                    java.net.http.HttpResponse.BodyHandlers.ofString()
            );

            // Test
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).startsWith(":root {");
        }
    }

    @Test
    void testCustomStylesLoadedAndApplied() throws InterruptedException, URISyntaxException, IOException {
        testWebserver();
        assertThat(counter.get()).isEqualTo(1);

        final var page = getPage();

        page.navigate("http://localhost:8081/");
        page.waitForFunction("""
              () => getComputedStyle(document.documentElement)
                  .getPropertyValue('--komunumo-background-color')
                  .trim() === 'lightblue'
              """);

        captureScreenshot("home-with-custom-styles");

        final var backgroundColor = page.evaluate("""
          () => getComputedStyle(document.querySelector('main'))
              .getPropertyValue('background-color')
          """).toString();

        assertThat(backgroundColor).isEqualTo("rgb(173, 216, 230)"); // lightblue
    }

}
