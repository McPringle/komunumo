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
package app.komunumo.ui.website.page;

import app.komunumo.ui.IntegrationTest;
import app.komunumo.ui.component.AbstractView;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import org.junit.jupiter.api.Test;

import static app.komunumo.util.TestUtil.findComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalPageViewIT extends IntegrationTest {

    @Test
    void aboutPage() {
        UI.getCurrent().navigate("page/about");

        final var div = _get(AbstractView.class, spec -> spec.withClasses("global-page-view"));
        final var html = findComponent(div, Html.class);
        assertThat(html).isNotNull();

        final var renderedHtml = html.getElement().getProperty("innerHTML");
        assertThat(renderedHtml).startsWith("<h2>About</h2>");
    }

    @Test
    void errorPage() {
        UI.getCurrent().navigate("page/non-existing");

        final var main = _get(Main.class);
        final var h2 = findComponent(main, H2.class);
        assertThat(h2).isNotNull();
        assertThat(h2.getText()).isEqualTo("Page not found");
    }

}
