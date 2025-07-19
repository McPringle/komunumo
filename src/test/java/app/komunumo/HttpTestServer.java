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
package app.komunumo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

public class HttpTestServer implements LauncherSessionListener {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(HttpTestServer.class);

    private static HttpServer server;

    @Override
    public void launcherSessionOpened(final @Nullable LauncherSession session) {
        try {
            final var root = Path.of("src/test/resources").toAbsolutePath().normalize();
            server = HttpServer.create(new InetSocketAddress(8082), 0);
            server.createContext("/", exchange -> handleRequest(exchange, root));
            server.setExecutor(null);
            server.start();
            LOGGER.info("[HTTP Server] Started on http://localhost:8082 serving {}", root);
        } catch (final @NotNull  IOException e) {
            throw new RuntimeException("Failed to start HTTP server", e);
        }
    }

    @Override
    public void launcherSessionClosed(final @Nullable LauncherSession session) {
        if (server != null) {
            server.stop(0);
            LOGGER.info("[HTTP Server] Stopped");
        }
    }

    private static void handleRequest(final @NotNull HttpExchange exchange,
                                      final @NotNull Path root) throws IOException {
        final var uriPath = exchange.getRequestURI().getPath();
        final var file = root.resolve("." + uriPath).normalize();

        if (!file.startsWith(root) || !Files.exists(file) || Files.isDirectory(file)) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        final var contentType = guessContentType(file);
        final var headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);

        final var body = Files.readAllBytes(file);
        exchange.sendResponseHeaders(200, body.length);

        try (final OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private static String guessContentType(final @NotNull Path path) {
        try {
            final var probe = Files.probeContentType(path);
            if (probe != null) {
                return probe;
            }
        } catch (final IOException ignored) {
            // if probeContentType fails, we fall back to manual guessing
        }

        final var name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".css")) return "text/css";
        if (name.endsWith(".js")) return "application/javascript";
        if (name.endsWith(".json")) return "application/json";
        if (name.endsWith(".svg")) return "image/svg+xml";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".gif")) return "image/gif";
        if (name.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }
}
