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
package app.komunumo.servlets.images;

import app.komunumo.data.dto.ContentType;
import app.komunumo.data.dto.ImageDto;
import app.komunumo.data.service.ImageService;
import app.komunumo.data.service.ServiceProvider;
import app.komunumo.util.ImageUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static app.komunumo.util.ImageUtil.extractImageIdFromUrl;

public final class ImageServlet extends HttpServlet {

    private static final @NotNull Pattern PLACEHOLDER_URL_PATTERN =
            Pattern.compile("^/placeholder-(\\d+)x(\\d+)\\.svg$");

    private final transient @NotNull PlaceholderImageGenerator placeholderImageGenerator;

    private static final long IMAGE_CACHE_DURATION = 86400; // 24 hours in seconds

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(ImageServlet.class);

    private final transient @NotNull ImageService imageService;

    public ImageServlet(final @NotNull ServiceProvider serviceProvider) {
        super();
        this.imageService = serviceProvider.imageService();
        this.placeholderImageGenerator = new PlaceholderImageGenerator(serviceProvider.getAppConfig());
    }

    @Override
    protected void doGet(final @NotNull HttpServletRequest request,
                         final @NotNull HttpServletResponse response) {
        final var url = request.getPathInfo();

        if (url == null) {
            redirectToNotFoundPage(request, response);
            return;
        }

        final var placeholderMatcher = PLACEHOLDER_URL_PATTERN.matcher(url);
        if (placeholderMatcher.find()) {
            final var imageWidth = Integer.parseInt(placeholderMatcher.group(1));
            final var imageHeight = Integer.parseInt(placeholderMatcher.group(2));
            if (imageWidth > 0 && imageHeight > 0) {
                generatePlaceholderImage(imageWidth, imageHeight, request, response);
                return;
            }
        }

        final UUID imageId = extractImageIdFromUrl(url);
        if (imageId == null) {
            redirectToNotFoundPage(request, response);
            return;
        }

        final Optional<ImageDto> imageOpt = imageService.getImage(imageId);
        if (imageOpt.isEmpty()) {
            redirectToNotFoundPage(request, response);
            return;
        }

        final var image = imageOpt.orElseThrow();
        final Optional<InputStream> stream = ImageUtil.loadImage(image);
        if (stream.isEmpty()) {
            LOGGER.error("Missing image on server: {}", image.id());
            redirectToInternalServerErrorPage(request, response);
            return;
        }

        response.setContentType(image.contentType().getContentType());
        response.setHeader("Cache-Control", "public, max-age=" + IMAGE_CACHE_DURATION);
        try (InputStream input = stream.orElseThrow(() ->
                new IOException("Unable to stream image from request '%s'!".formatted(request.getPathInfo())))) {
            input.transferTo(response.getOutputStream());
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
            redirectToInternalServerErrorPage(request, response);
        }
    }

    private void generatePlaceholderImage(final int imageWidth, final int imageHeight,
                                          final @NotNull HttpServletRequest request,
                                          final @NotNull HttpServletResponse response) {
        final var placeholderImage = placeholderImageGenerator.getPlaceholderImage(imageWidth, imageHeight);

        // set response headers
        response.setContentType(ContentType.IMAGE_SVG.getContentType());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Cache-Control", "public, max-age=" + IMAGE_CACHE_DURATION);

        // stream the placeholder image
        try (PrintWriter out = response.getWriter()) {
            out.write(placeholderImage);
        } catch (final IOException e) {
            LOGGER.error("Unable to stream placeholder image: {}", e.getMessage(), e);
            redirectToInternalServerErrorPage(request, response);
        }
    }

    private void redirectToNotFoundPage(final @NotNull HttpServletRequest request,
                                        final @NotNull HttpServletResponse response) {
        LOGGER.warn("Requested image not found: {}", request.getPathInfo());
        try {
            response.sendRedirect("/error/404");
        } catch (final IOException e) {
            LOGGER.error("Redirect to 404 page failed", e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void redirectToInternalServerErrorPage(final @NotNull HttpServletRequest request,
                                                   final @NotNull HttpServletResponse response) {
        LOGGER.warn("Internal Server Error handling request: {}", request.getPathInfo());
        try {
            response.sendRedirect("/error/500");
        } catch (final IOException e) {
            LOGGER.error("Redirect to 500 page failed", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
