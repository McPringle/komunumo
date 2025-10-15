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
package app.komunumo.ui.views.community;

import app.komunumo.data.service.ConfigurationService;
import app.komunumo.ui.KaribuTest;
import app.komunumo.ui.components.NavigationBar;
import app.komunumo.ui.views.WebsiteLayout;
import com.icegreen.greenmail.store.FolderException;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.router.RouterLink;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static app.komunumo.data.dto.ConfigurationSetting.INSTANCE_HIDE_COMMUNITIES;
import static app.komunumo.util.TestUtil.assertContainsExactlyOneRouterLinkOf;
import static app.komunumo.util.TestUtil.findComponent;
import static app.komunumo.util.TestUtil.findComponents;
import static org.assertj.core.api.Assertions.assertThat;

class HideCommunitiesKT extends KaribuTest {

    @Autowired
    private ConfigurationService configurationService;

    @Test
    void checkCommunityLinkNotVisible() throws FolderException {
        try {
            configurationService.setConfiguration(INSTANCE_HIDE_COMMUNITIES, true);

            // simulate a full browser-reload by re-initializing the UI to apply the configuration change
            tearDownMockVaadin();
            setupMockVaadin();

            final var uiParent = UI.getCurrent()
                    .getCurrentView()
                    .getParent().orElseThrow()
                    .getParent().orElseThrow();
            final var websiteLayout = (WebsiteLayout) uiParent;

            final var navigationBar = findComponent(websiteLayout, NavigationBar.class);
            assertThat(navigationBar).isNotNull();

            final var routerLinks = findComponents(navigationBar, RouterLink.class);
            assertContainsExactlyOneRouterLinkOf(routerLinks, new Anchor("events", "Events"));
        } finally {
            configurationService.setConfiguration(INSTANCE_HIDE_COMMUNITIES, false);
        }
    }

}
