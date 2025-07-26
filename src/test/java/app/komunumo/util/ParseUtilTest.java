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

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ParseUtilTest {

    @Test
    void parseUUID() {
        assertThat(ParseUtil.parseUUID("8232a4f1-3f02-4db3-bf78-18387734c81c"))
                .isPresent()
                .hasValue(UUID.fromString("8232a4f1-3f02-4db3-bf78-18387734c81c"));
        assertThat(ParseUtil.parseUUID("invalid-uuid")).isEmpty();
        assertThat(ParseUtil.parseUUID("")).isEmpty();
        assertThat(ParseUtil.parseUUID((String) null)).isEmpty();
    }

    @Test
    void testParseUUID() {
        assertThat(ParseUtil.parseUUID(Optional.of("8232a4f1-3f02-4db3-bf78-18387734c81c")))
                .isPresent()
                .hasValue(UUID.fromString("8232a4f1-3f02-4db3-bf78-18387734c81c"));
        assertThat(ParseUtil.parseUUID(Optional.of("invalid-uuid"))).isEmpty();
        assertThat(ParseUtil.parseUUID(Optional.of(""))).isEmpty();
        assertThat(ParseUtil.parseUUID(Optional.empty())).isEmpty();
    }

}
