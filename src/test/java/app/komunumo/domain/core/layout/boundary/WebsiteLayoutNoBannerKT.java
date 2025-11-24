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
package app.komunumo.domain.core.layout.boundary;

import app.komunumo.test.KaribuTest;
import app.komunumo.vaadin.components.InfoBanner;
import com.vaadin.flow.component.UI;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static app.komunumo.test.TestUtil.findComponent;
import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "komunumo.demo.enabled=false")
class WebsiteLayoutNoBannerKT extends KaribuTest {

    private @NotNull WebsiteLayout websiteLayout;

    @BeforeEach
    void setUp() {
        UI.getCurrent().navigate(RootView.class);
        final var uiParent = UI.getCurrent()
                .getCurrentView()
                .getParent().orElseThrow()
                .getParent().orElseThrow();
        websiteLayout = (WebsiteLayout) uiParent;
    }

    @Test
    void testLayoutHasNoBanner()  {
        final var infoBanner = findComponent(websiteLayout, InfoBanner.class);
        assertThat(infoBanner).isNull();
    }

}
