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
package org.komunumo.data.converter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Converter;
import org.komunumo.data.dto.ContentType;

public final class ContentTypeConverter implements Converter<String, ContentType> {

    @Override
    public ContentType from(@Nullable final String contentType) {
        return contentType == null ? null : ContentType.fromContentType(contentType);
    }

    @Override
    public String to(@Nullable final ContentType contentType) {
        return contentType == null ? null : contentType.getContentType();
    }

    @Override
    @NotNull
    public Class<String> fromType() {
        return String.class;
    }

    @Override
    @NotNull
    public Class<ContentType> toType() {
        return ContentType.class;
    }
}
