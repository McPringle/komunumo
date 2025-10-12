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

import app.komunumo.configuration.AppConfig;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static app.komunumo.util.ResourceUtil.getResourceAsString;
import static app.komunumo.util.TemplateUtil.replaceVariables;

final class PlaceholderImageGenerator {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(PlaceholderImageGenerator.class);

    private static final @NotNull String PLACEHOLDER_IMAGE_TEMPLATE_FILE = "/META-INF/resources/images/placeholder.svg";
    private static final @NotNull String FALLBACK_PLACEHOLDER_IMAGE_TEMPLATE = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <svg xmlns="http://www.w3.org/2000/svg" width="${imageWidth}" height="${imageHeight}">
                <rect width="100%" height="100%" fill="#d0d7de" />
                <g id="Logo" />
            </svg>""";

    private static final @NotNull String KOMUNUMO_LOGO_FILE = "/META-INF/resources/images/komunumo.svg";

    private final int baseLogoWidth;
    private final int baseLogoHeight;
    private final double baseLogoAspectRatio;

    private final @NotNull SvgHelper templateApplier;
    private final @NotNull String placeholderImageTemplate;

    // Cache for recently generated placeholder images
    private final @NotNull Cache<@NotNull CacheKey, @NotNull String> imageCache = Caffeine.newBuilder()
            .maximumSize(100)
            .build();

    PlaceholderImageGenerator(final @NotNull AppConfig appConfig) {
        final var instanceLogo = loadInstanceLogo(appConfig.files().basedir());

        this.templateApplier = new SvgHelper(instanceLogo);
        final var placeholderImageRaw = getResourceAsString(PLACEHOLDER_IMAGE_TEMPLATE_FILE, FALLBACK_PLACEHOLDER_IMAGE_TEMPLATE);
        this.placeholderImageTemplate = templateApplier.parseTemplate(placeholderImageRaw);

        this.baseLogoWidth = (int) templateApplier.getUserSvgWidth();
        this.baseLogoHeight = (int) templateApplier.getUserSvgHeight();
        this.baseLogoAspectRatio = (double) baseLogoWidth / baseLogoHeight;
    }

    private String loadInstanceLogo(final @NotNull Path basedir) {
        final var customLogoPath = basedir.resolve(Path.of("custom", "images", "logo.svg"));
        if (Files.exists(customLogoPath)) {
            try {
                final var customLogo = Files.readString(customLogoPath);
                LOGGER.info("Custom logo found and successfully loaded.");
                return customLogo;
            } catch (final IOException e) {
                LOGGER.warn("Failed to read custom logo from '{}', fallback to default logo.", customLogoPath, e);
            }
        } else {
            LOGGER.info("No custom logo found, using default logo.");
        }

        final var defaultLogo = getResourceAsString(KOMUNUMO_LOGO_FILE, "");
        if (defaultLogo.isBlank()) {
            LOGGER.warn("Failed to read default logo from '{}', fallback to empty logo.", KOMUNUMO_LOGO_FILE);
        }
        return defaultLogo;
    }

    /**
     * <p>Returns a placeholder image SVG code for the given dimensions.
     * The image is cached to avoid generating it multiple times.</p>
     *
     * @param imageWidth  the width of the placeholder image
     * @param imageHeight the height of the placeholder image
     * @return the SVG code for the placeholder image
     */
    String getPlaceholderImage(final int imageWidth, final int imageHeight) {
        final var cacheKey = new CacheKey(imageWidth, imageHeight);
        return imageCache.get(cacheKey, _ -> generatePlaceholderImage(imageWidth, imageHeight));
    }

    @SuppressWarnings("ExtractMethodRecommender")
    private String generatePlaceholderImage(final int imageWidth, final int imageHeight) {
        // maximum permitted size (50% of the dimension)
        final int percentage = 50;
        final int maxLogoHeight = percentage * imageHeight / 100;
        final int maxLogoWidth = percentage * imageWidth / 100;

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
        final double logoPositionX = (imageWidth - logoScalingFactor * baseLogoWidth) / 2.0;
        final double logoPositionY = (imageHeight - logoScalingFactor * baseLogoHeight) / 2.0;

        // generate the placeholder SVG code
        final var variables = Map.of(
                "imageWidth", String.valueOf(imageWidth),
                "imageHeight", String.valueOf(imageHeight),
                "logoPositionX", String.valueOf(logoPositionX),
                "logoPositionY", String.valueOf(logoPositionY),
                "logoScalingFactor", String.valueOf(logoScalingFactor));
        final var applicableImageTemplate = replaceVariables(placeholderImageTemplate, variables);
        return templateApplier.applyTemplate(applicableImageTemplate);
    }

    private record CacheKey(int imageWidth, int imageHeight) { }

}
