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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DownloadUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadUtil.class);

    public static @NotNull String getString(final @NotNull String location)
            throws IOException, URISyntaxException {
        try (InputStream in = new URI(location).toURL().openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @SuppressWarnings({"java:S2095", "java:S2142", "LoggingSimilarMessage"})
    public static @Nullable Path downloadFile(final @NotNull String location) {
        try {
            final var tempFile = Files.createTempFile("download-", ".tmp");
            tempFile.toFile().deleteOnExit();

            final HttpClient client = HttpClient.newHttpClient();
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(location))
                    .GET()
                    .build();
            final var response = client.send(request, HttpResponse.BodyHandlers.ofFile(tempFile));
            final var statusCode = response.statusCode();
            if (statusCode == 200) {
                return tempFile;
            } else {
                LOGGER.error("Failed to download file from '{}': HTTP status code {}", location, statusCode);
                Files.deleteIfExists(tempFile);
            }
        } catch (final IOException | InterruptedException e) {
            LOGGER.error("Failed to download file from '{}': {}", location, e.getMessage());
        }
        return null;
    }

    private DownloadUtil() {
        throw new IllegalStateException("Utility class");
    }

}
