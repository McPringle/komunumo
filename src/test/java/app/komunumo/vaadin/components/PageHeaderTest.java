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
package app.komunumo.vaadin.components;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static app.komunumo.test.TestUtil.findComponent;

class PageHeaderTest {

    @Test
    void onlyTitle() {
        final var header = new PageHeader("Komunumo", null);
        assertThat(header.getClassNames()).contains("page-header");

        final var h1 = findComponent(header, H1.class);
        assertThat(h1).isNotNull();
        assertThat(h1.getText()).isEqualTo("Komunumo");

        assertThat(findComponent(header, H2.class)).isNull();
    }

    @Test
    void titleWithSubtitle() {
        final var header = new PageHeader("Komunumo", "Open Source Community Manager");
        assertThat(header.getClassNames()).contains("page-header");

        final var h1 = findComponent(header, H1.class);
        assertThat(h1).isNotNull();
        assertThat(h1.getText()).isEqualTo("Komunumo");

        final var h2 = findComponent(header, H2.class);
        assertThat(h2).isNotNull();
        assertThat(h2.getText()).isEqualTo("Open Source Community Manager");
    }

}
