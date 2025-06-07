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

import app.komunumo.configuration.AppConfig;
import app.komunumo.data.dto.ImageDto;
import app.komunumo.data.service.ImageService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ImageUtil {

    private static final @NotNull String IMAGE_URL = "/images";
    private static final @NotNull Path RELATIVE_IMAGE_PATH = Path.of("uploads", "images");
    private static final @NotNull Pattern UUID_EXTRACT_PATTERN = Pattern.compile(
            ".*/([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})\\.");

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(ImageUtil.class);
    private static Path uploadImagePath;

    public static void initialize(final @NotNull AppConfig appConfig) {
        uploadImagePath = appConfig.files().basedir().resolve(RELATIVE_IMAGE_PATH);
    }

    public static @Nullable String resolveImageUrl(final @Nullable ImageDto image) {
        if (image == null || image.id() == null) {
            return null;
        }
        final String id = image.id().toString();
        return Path.of(IMAGE_URL,
                id + image.contentType().getExtension())
                .toString();

    }

    public static @Nullable Path resolveImagePath(final @Nullable ImageDto image) {
        if (image == null || image.id() == null) {
            return null;
        }
        final String id = image.id().toString();
        final String prefix1 = id.substring(0, 2);
        final String prefix2 = id.substring(2, 4);
        return uploadImagePath
                .resolve(prefix1)
                .resolve(prefix2)
                .resolve(id + image.contentType().getExtension());

    }

    public static @NotNull Optional<@NotNull InputStream> loadImage(final @Nullable ImageDto image) {
        final var path = resolveImagePath(image);
        if (path == null || !Files.exists(path)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Files.newInputStream(path));
        } catch (final IOException e) {
            LOGGER.warn("Failed to load image '{}': {}", path, e.getMessage(), e);
            return Optional.empty();
        }
    }

    public static @Nullable UUID extractImageIdFromUrl(final @NotNull String url) {
        final var matcher = UUID_EXTRACT_PATTERN.matcher(url);
        if (matcher.find()) {
            final var uuidStr = matcher.group(1);
            return UUID.fromString(uuidStr);
        }
        return null;
    }

    public static void storeImage(final @NotNull ImageDto image, final @NotNull Path path) throws IOException {
        final UUID imageId = image.id();
        if (imageId == null) {
            throw new IllegalArgumentException("ImageDto must have an ID!");
        }

        final String id = imageId.toString();
        final String prefix1 = id.substring(0, 2);
        final String prefix2 = id.substring(2, 4);
        final Path targetDir = uploadImagePath.resolve(prefix1).resolve(prefix2);
        final Path targetFile = targetDir.resolve(id + image.contentType().getExtension());

        Files.createDirectories(targetDir);
        Files.move(path, targetFile, StandardCopyOption.REPLACE_EXISTING);

        LOGGER.info("Stored image '{}' as '{}'", path.toAbsolutePath(), targetFile.toAbsolutePath());
    }

    public static void cleanupOrphanedImageFiles(final @NotNull ImageService imageService) {
        try {
            final List<UUID> knownImageIds = imageService.getAllImageIds();
            try (var files = Files.walk(uploadImagePath)) {
                files
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            final var filename = path.getFileName().toString();
                            final int dotIndex = filename.lastIndexOf('.');
                            final var uuidPart = filename.substring(0, dotIndex);
                            try {
                                final var imageId = UUID.fromString(uuidPart);
                                if (!knownImageIds.contains(imageId)) {
                                    Files.delete(path);
                                    LOGGER.info("Deleted orphaned image file: {}", path);
                                }
                            } catch (final @NotNull IllegalArgumentException e) {
                                LOGGER.warn("Skipping file with invalid UUID: {}", filename);
                            } catch (final @NotNull IOException e) {
                                LOGGER.warn("Could not delete file {}: {}", path, e.getMessage());
                            }
                        });
            }

            try (var dirs = Files.walk(uploadImagePath)) {
                dirs.sorted(Comparator.reverseOrder())
                        .filter(Files::isDirectory)
                        .forEach(dir -> {
                            try (Stream<Path> entries = Files.list(dir)) {
                                if (entries.findAny().isEmpty()) {
                                    Files.delete(dir);
                                    LOGGER.info("Deleted empty directory: {}", dir);
                                }
                            } catch (final @NotNull IOException e) {
                                LOGGER.warn("Could not inspect or delete directory {}: {}", dir, e.getMessage());
                            }
                        });
            }
        } catch (final @NotNull Exception e) {
            LOGGER.error("Error while cleaning up orphaned image files: {}", e.getMessage(), e);
        }
    }

    private ImageUtil() {
        throw new IllegalStateException("Utility class");
    }

}
