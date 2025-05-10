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
package org.komunumo.ui.component;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.komunumo.ui.KaribuTestBase.findComponent;

class PageHeaderTest {

    @Test
    void onlyTitle() {
        final var header = new PageHeader("Komunumo", null);
        assertTrue(header.hasClassName("page-header"));

        final var h1 = findComponent(header, H1.class);
        assertNotNull(h1);
        assertEquals("Komunumo", h1.getText());

        assertNull(findComponent(header, H2.class));
    }

    @Test
    void titleWithSubtitle() {
        final var header = new PageHeader("Komunumo", "Open Source Community Management");
        assertTrue(header.hasClassName("page-header"));

        final var h1 = findComponent(header, H1.class);
        assertNotNull(h1);
        assertEquals("Komunumo", h1.getText());

        final var h2 = findComponent(header, H2.class);
        assertNotNull(h2);
        assertEquals("Open Source Community Management", h2.getText());
    }

}
