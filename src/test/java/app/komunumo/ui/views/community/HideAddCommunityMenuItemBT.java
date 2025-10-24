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

import app.komunumo.data.dto.UserType;
import app.komunumo.data.service.ConfigurationService;
import app.komunumo.ui.BrowserTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static app.komunumo.data.dto.ConfigurationSetting.INSTANCE_ADD_COMMUNITY_ALLOWED;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author smzoha
 * @since 25/10/25
 **/
public class HideAddCommunityMenuItemBT extends BrowserTest {

    private static final String CREATE_COMMUNITY_MENU_ITEM_SELECTOR =
            "vaadin-context-menu-item[role='menuitem']:has-text('Create Community')";

    @Autowired
    private ConfigurationService configurationService;


    @Test
    void checkCreateCommunityMenuItemNotVisible_WhenConfigurationIsFalse() {
        final var localUser = getTestUser(UserType.LOCAL);
        login(localUser);

        configurationService.setConfiguration(INSTANCE_ADD_COMMUNITY_ALLOWED, false);

        final var page = getPage();

        page.navigate(getInstanceUrl());
        page.waitForSelector(INSTANCE_NAME_SELECTOR);

        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
        captureScreenshot("createCommunityMenuNotVisibleForLocalUser");

        final var menuItem = page.locator(CREATE_COMMUNITY_MENU_ITEM_SELECTOR);
        assertThat(menuItem.isVisible()).isFalse();
    }
}
