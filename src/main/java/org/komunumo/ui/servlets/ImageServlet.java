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
package org.komunumo.ui.servlets;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.dto.ImageDto;
import org.komunumo.data.service.ImageService;
import org.komunumo.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.komunumo.util.ImageUtil.extractImageIdFromUrl;

public final class ImageServlet extends HttpServlet {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(ImageServlet.class);

    private final transient @NotNull ImageService imageService;

    public ImageServlet(final @NotNull ImageService imageService) {
        super();
        this.imageService = imageService;
    }

    @Override
    protected void doGet(final @NotNull HttpServletRequest request,
                         final @NotNull HttpServletResponse response) {
        final var url = request.getPathInfo();
        final UUID imageId = extractImageIdFromUrl(url);
        if (imageId == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final ImageDto image = imageService
                .getImage(imageId)
                .orElse(null);
        if (image == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final Optional<InputStream> stream = ImageUtil.loadImage(image);
        if (stream.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(image.contentType().getContentType());
        response.setHeader("Cache-Control", "public, max-age=86400"); // allow caching for 24h
        try (InputStream input = stream.orElseThrow(() ->
                new IOException("Unable to stream image from request '%s'!".formatted(request.getPathInfo())))) {
            input.transferTo(response.getOutputStream());
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
