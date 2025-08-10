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
package app.komunumo.ui.servlets;

import app.komunumo.data.dto.ContentType;
import app.komunumo.data.dto.ImageDto;
import app.komunumo.data.service.ImageService;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static app.komunumo.util.ImageUtil.extractImageIdFromUrl;
import static app.komunumo.util.ResourceUtil.getResourceAsString;
import static app.komunumo.util.TemplateUtil.replaceVariables;

public final class ImageServlet extends HttpServlet {

    private static final @NotNull String PLACEHOLDER_IMAGE_TEMPLATE_FILE = "/META-INF/resources/images/placeholder.svg";
    private static final @NotNull String KOMUNUMO_LOGO_FILE = "/META-INF/resources/images/komunumo.svg";

    private static final @NotNull String FALLBACK_PLACEHOLDER_IMAGE_TEMPLATE = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <svg xmlns="http://www.w3.org/2000/svg" width="${imageWidth}" height="${imageHeight}">
                <rect width="100%" height="100%" fill="#d0d7de" />
                <g id="Logo" />
            </svg>""";

    private static final @NotNull Pattern PLACEHOLDER_URL_PATTERN =
            Pattern.compile("^/placeholder-(\\d+)x(\\d+)\\.svg$");

    private final int baseLogoWidth;
    private final int baseLogoHeight;
    private final double baseLogoAspectRatio;

    private final transient SvgTemplateApplier templateApplier;

    private static final long IMAGE_CACHE_DURATION = 86400; // 24 hours in seconds

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(ImageServlet.class);

    private final transient @NotNull ImageService imageService;

    private final @NotNull String placeholderImageTemplate;

    public ImageServlet(final @NotNull ImageService imageService) {
        this(imageService, PLACEHOLDER_IMAGE_TEMPLATE_FILE, "");
    }

    public ImageServlet(final @NotNull ImageService imageService,
                        final @NotNull String instanceLogoPath) {
        this(imageService, PLACEHOLDER_IMAGE_TEMPLATE_FILE, instanceLogoPath);
    }

    public ImageServlet(final @NotNull ImageService imageService,
                        final @NotNull String placeholderImageFile,
                        final @NotNull String instanceLogoPath) {
        super();
        this.imageService = imageService;
        var placeholderImageRaw = getResourceAsString(placeholderImageFile, FALLBACK_PLACEHOLDER_IMAGE_TEMPLATE);

        try {
            this.templateApplier = new SvgTemplateApplier(instanceLogoPath, KOMUNUMO_LOGO_FILE);
            this.placeholderImageTemplate = templateApplier.parseTemplate(placeholderImageRaw);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.baseLogoWidth = (int) templateApplier.getUserSvgWidth();
        this.baseLogoHeight = (int) templateApplier.getUserSvgHeight();
        this.baseLogoAspectRatio = (double) baseLogoWidth / baseLogoHeight;

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
            final var width = Integer.parseInt(placeholderMatcher.group(1));
            final var height = Integer.parseInt(placeholderMatcher.group(2));
            if (width > 0 && height > 0) {
                final var imageDimension = new ImageDimension(width, height);
                generatePlaceholderImage(imageDimension, request, response);
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

    private void generatePlaceholderImage(final @NotNull ImageDimension dimension,
                                          final @NotNull HttpServletRequest request,
                                          final @NotNull HttpServletResponse response) {
        // maximum permitted size (50% of the dimension)
        final int percentage = 50;
        final int maxLogoHeight = percentage * dimension.height / 100;
        final int maxLogoWidth = percentage * dimension.width / 100;

        // calculate scaled height and width, limited to both dimensions
        final int logoWidthByHeight = (int) Math.round(maxLogoHeight * baseLogoAspectRatio);
        final int logoHeightByWidth = (int) Math.round(maxLogoWidth / baseLogoAspectRatio);

        // choose the variant that fits in both directions
        int logoHeight;
        if (logoWidthByHeight <= maxLogoWidth) {
            logoHeight = maxLogoHeight;
        } else {
            logoHeight = logoHeightByWidth;
        }

        // centering
        final double logoScalingFactor = (double) logoHeight / baseLogoHeight;
        final double logoPositionX = (dimension.width - logoScalingFactor * baseLogoWidth) / 2.0;
        final double logoPositionY = (dimension.height - logoScalingFactor * baseLogoHeight) / 2.0;

        // generate the placeholder SVG code
        final var variables = Map.of(
                "imageWidth", String.valueOf(dimension.width),
                "imageHeight", String.valueOf(dimension.height),
                "logoPositionX", String.valueOf(logoPositionX),
                "logoPositionY", String.valueOf(logoPositionY),
                "logoScalingFactor", String.valueOf(logoScalingFactor));
        final var applyableImageTemplate = replaceVariables(placeholderImageTemplate, variables);
        final var placeholderImage = templateApplier.applyTemplate(applyableImageTemplate);

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

    private record ImageDimension(int width, int height) { }

}
