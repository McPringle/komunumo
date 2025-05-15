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
package org.komunumo;

import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public final class ApplicationScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationScheduler.class);

    private final DatabaseService databaseService;

    public ApplicationScheduler(final @NotNull  DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupOrphanedImages() {
        LOGGER.info("Cleaning up orphaned images...");
        databaseService.findOrphanedImages().forEach(image -> {
            LOGGER.info("Deleting orphaned image with ID {} from filesystem.", image.id());
            // TODO delete image from filesystem
            LOGGER.info("Deleting orphaned image with ID {} from database.", image.id());
            databaseService.deleteImage(image);
        });
        LOGGER.info("Orphaned images cleaned.");
    }

}
