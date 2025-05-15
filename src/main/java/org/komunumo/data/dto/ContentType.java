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
package org.komunumo.data.dto;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ContentType {
    IMAGE_GIF("image/gif", ".gif"),
    IMAGE_JPEG("image/jpeg", ".jpg"),
    IMAGE_PNG("image/png", ".png"),
    IMAGE_SVG("image/svg+xml", ".svg"),
    IMAGE_WEBP("image/webp", ".webp"),;

    private final @NotNull String contentType;
    private final @NotNull String extension;

    ContentType(final @NotNull String contentType, final @NotNull String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public static ContentType fromContentType(final @Nullable String contentType) {
        for (ContentType imageType : values()) {
            if (imageType.contentType.equals(contentType)) {
                return imageType;
            }
        }
        throw new IllegalArgumentException("Unknown content type: " + contentType);
    }

    public @NotNull String getContentType() {
        return contentType;
    }

    public @NotNull String getExtension() {
        return extension;
    }

}
