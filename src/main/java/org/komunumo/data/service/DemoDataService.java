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
package org.komunumo.data.service;

import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.komunumo.data.dto.CommunityDto;
import org.komunumo.data.dto.ContentType;
import org.komunumo.data.dto.ImageDto;
import org.komunumo.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public final class DemoDataService {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(DemoDataService.class);

    private final @NotNull DatabaseService databaseService;

    public DemoDataService(final @NotNull DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    /**
     * Initializes demo data after application startup.
     * <p>
     * This method is executed automatically once the Spring context is fully initialized.
     * It checks whether any communities exist in the database, and if none are found,
     * it inserts a predefined set of demo communities with predictable IDs.
     * </p>
     */
    @PostConstruct
    public void createDemoData() {
        LOGGER.info("Creating demo data...");

        if (databaseService.getImageCount() == 0) {
            for (int i = 1; i <= 5; i++) {
                final var filename = "demo-background-" + i + ".jpg";
                final var image = databaseService.storeImage(new ImageDto(
                        generateId(Integer.toString(i)), ContentType.IMAGE_JPEG,
                        filename));
                storeDemoImage(image, filename);
            }
        }

        if (databaseService.getCommunityCount() == 0) {
            for (int i = 1; i <= 6; i++) {
                final var generatedId = generateId(Integer.toString(i));
                databaseService.storeCommunity(new CommunityDto(
                        generatedId, "@demoGroup" + i, null, null,
                        "Demo Community " + i, "This is a demo community.",
                        i <= 5 ? generatedId : null)); // demo community 6+ has no image
            }
        }

        LOGGER.info("Demo data created.");
    }

    /**
     * Generates a predictable ID based on a given part.
     * The ID consists of repeated parts of the input,
     * formatted to match the standard UUID structure.
     *
     * @param part the part to base the ID on
     * @return an ID with a predictable pattern
     */
    private @NotNull UUID generateId(final String part) {
        // Repeat the part until it reaches at least 32 characters
        final var hexBuilder = new StringBuilder();
        while (hexBuilder.length() < 32) {
            hexBuilder.append(part);
        }

        // Trim to exactly 32 characters
        final var hex = hexBuilder.substring(0, 32);

        // Format the string to UUID pattern: 8-4-4-4-12
        final var idStr = String.format(
                "%s-%s-%s-%s-%s",
                hex.substring(0, 8),
                hex.substring(8, 12),
                hex.substring(12, 16),
                hex.substring(16, 20),
                hex.substring(20, 32)
        );

        return UUID.fromString(idStr);
    }

    @VisibleForTesting
    void storeDemoImage(final ImageDto image, final String filename) {
        // load resources from classpath
        try (InputStream inputStream = getClass().getResourceAsStream("/META-INF/resources/images/" + filename)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Demo image not found: " + filename);
            }

            // write them to a temporary file (for transfer to file-based API)
            final var tempFile = Files.createTempFile("demo-image-", ".jpg");
            tempFile.toFile().deleteOnExit();
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            // finally: store the image
            ImageUtil.storeImage(image, tempFile);
        } catch (final IOException e) {
            LOGGER.warn(e.getMessage());
        }
    }

}
