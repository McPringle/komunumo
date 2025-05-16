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
package app.komunumo.data.converter;

import org.junit.jupiter.api.Test;
import app.komunumo.data.dto.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContentTypeConverterTest {

    @Test
    void from() {
        final var converter = new ContentTypeConverter();
        assertThat(converter.from(null)).isNull();
        assertThat(converter.from("image/gif")).isEqualTo(ContentType.IMAGE_GIF);
        assertThat(converter.from("image/jpeg")).isEqualTo(ContentType.IMAGE_JPEG);
        assertThat(converter.from("image/png")).isEqualTo(ContentType.IMAGE_PNG);
        assertThat(converter.from("image/svg+xml")).isEqualTo(ContentType.IMAGE_SVG);
        assertThat(converter.from("image/webp")).isEqualTo(ContentType.IMAGE_WEBP);

        assertThatThrownBy(() -> converter.from("unknown/type"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown content type: unknown/type");
    }

    @Test
    @SuppressWarnings("ConstantValue")
    void to() {
        final var converter = new ContentTypeConverter();
        assertThat(converter.to(null)).isNull();
        assertThat(converter.to(ContentType.IMAGE_GIF)).isEqualTo("image/gif");
        assertThat(converter.to(ContentType.IMAGE_JPEG)).isEqualTo("image/jpeg");
        assertThat(converter.to(ContentType.IMAGE_PNG)).isEqualTo("image/png");
        assertThat(converter.to(ContentType.IMAGE_SVG)).isEqualTo("image/svg+xml");
        assertThat(converter.to(ContentType.IMAGE_WEBP)).isEqualTo("image/webp");
    }

    @Test
    void fromType() {
        final var converter = new ContentTypeConverter();
        assertThat(converter.fromType()).isEqualTo(String.class);
    }

    @Test
    void toType() {
        final var converter = new ContentTypeConverter();
        assertThat(converter.toType()).isEqualTo(ContentType.class);
    }

}
