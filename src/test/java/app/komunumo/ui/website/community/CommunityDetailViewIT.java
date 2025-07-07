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
package app.komunumo.ui.website.community;

import app.komunumo.ui.IntegrationTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import org.junit.jupiter.api.Test;

import static app.komunumo.util.TestUtil.findComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._find;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;

class CommunityDetailViewIT extends IntegrationTest {

    @Test
    void communityWithImage() {
        UI.getCurrent().navigate("communities/@demoCommunity1");

        final var h2 = _get(H2.class, spec -> spec.withClasses("community-name"));
        assertThat(h2).isNotNull();
        assertThat(h2.getText()).isEqualTo("Demo Community 1");

        final var created = _get(Paragraph.class, spec -> spec.withClasses("community-created"));
        assertThat(created).isNotNull();
        assertThat(created.getText()).isEqualTo("created moments ago");

        final var image = _get(Image.class, spec -> spec.withClasses("community-image"));
        assertThat(image).isNotNull();
        assertThat(image.getSrc()).isNotBlank().startsWith("/images/");
        assertThat(image.getAlt().orElseThrow()).isEqualTo("Profile picture of Demo Community 1");
    }

    @Test
    void communityWithoutImage() {
        UI.getCurrent().navigate("communities/@demoCommunity6");

        final var h2 = _get(H2.class, spec -> spec.withClasses("community-name"));
        assertThat(h2).isNotNull();
        assertThat(h2.getText()).isEqualTo("Demo Community 6");

        final var image = _find(Image.class, spec -> spec.withClasses("community-image"));
        assertThat(image).isEmpty();
    }

    @Test
    void nonExistingCommunity() {
        UI.getCurrent().navigate("communities/@nonExisting");

        final var main = _get(Main.class);
        final var h2 = findComponent(main, H2.class);
        assertThat(h2).isNotNull();
        assertThat(h2.getText()).isEqualTo("Page not found");
    }

}
