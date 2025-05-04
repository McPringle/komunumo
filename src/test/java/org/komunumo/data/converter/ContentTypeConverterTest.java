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

import org.junit.jupiter.api.Test;
import org.komunumo.data.dto.ContentType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContentTypeConverterTest {

    @Test
    void from() {
        final var converter = new ContentTypeConverter();
        assertNull(converter.from(null));
        assertEquals(ContentType.IMAGE_GIF, converter.from("image/gif"));
        assertEquals(ContentType.IMAGE_JPEG, converter.from("image/jpeg"));
        assertEquals(ContentType.IMAGE_PNG, converter.from("image/png"));
        assertEquals(ContentType.IMAGE_SVG, converter.from("image/svg+xml"));
        assertEquals(ContentType.IMAGE_WEBP, converter.from("image/webp"));

        final var expectedException = assertThrows(IllegalArgumentException.class, () -> converter.from("unknown/type"));
        assertEquals("Unknown content type: unknown/type", expectedException.getMessage());
    }

    @Test
    @SuppressWarnings("ConstantValue")
    void to() {
        final var converter = new ContentTypeConverter();
        assertNull(converter.to(null));
        assertEquals("image/gif", converter.to(ContentType.IMAGE_GIF));
        assertEquals("image/jpeg", converter.to(ContentType.IMAGE_JPEG));
        assertEquals("image/png", converter.to(ContentType.IMAGE_PNG));
        assertEquals("image/svg+xml", converter.to(ContentType.IMAGE_SVG));
        assertEquals("image/webp", converter.to(ContentType.IMAGE_WEBP));
    }

    @Test
    void fromType() {
        final var converter = new ContentTypeConverter();
        assertEquals(String.class, converter.fromType());
    }

    @Test
    void toType() {
        final var converter = new ContentTypeConverter();
        assertEquals(ContentType.class, converter.toType());
    }

}
