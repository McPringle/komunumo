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
package org.komunumo.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormatterUtilTest {

    @Test
    void testFormatNumber() {
        assertEquals("1", FormatterUtil.formatNumber(1L));
        assertEquals("12", FormatterUtil.formatNumber(12L));
        assertEquals("123", FormatterUtil.formatNumber(123L));
        assertEquals("1'234", FormatterUtil.formatNumber(1234L));
        assertEquals("12'345", FormatterUtil.formatNumber(12345L));
        assertEquals("123'456", FormatterUtil.formatNumber(123456L));
        assertEquals("1'234'567", FormatterUtil.formatNumber(1234567L));
        assertEquals("12'345'678", FormatterUtil.formatNumber(12345678L));
        assertEquals("123'456'789", FormatterUtil.formatNumber(123456789L));
    }

    @Test
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration") // this is exactly what we want to test
    void privateConstructorWithException() {
        final var cause = assertThrows(InvocationTargetException.class, () -> {
            Constructor<FormatterUtil> constructor = FormatterUtil.class.getDeclaredConstructor();
            if (Modifier.isPrivate(constructor.getModifiers())) {
                constructor.setAccessible(true);
                constructor.newInstance();
            }
        }).getCause();
        assertInstanceOf(IllegalStateException.class, cause);
        assertEquals("Utility class", cause.getMessage());
    }

}
