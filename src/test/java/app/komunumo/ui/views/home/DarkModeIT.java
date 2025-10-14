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
package app.komunumo.ui.views.home;

import app.komunumo.ui.BrowserTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DarkModeIT extends BrowserTest {

    protected static final String INSTANCE_NAME_SELECTOR = "h1:has-text('Your Instance Name')";
    protected static final String AVATAR_SELECTOR = "vaadin-avatar";
    protected static final String DARK_MODE_MENU_ITEM_SELECTOR = "vaadin-context-menu-item[role='menuitem']:has-text('Toggle Dark Mode')";
    protected static final String TITLE_COLOR_DARK_MODE = "rgb(245, 249, 255)";
    protected static final String TITLE_COLOR_LIGHT_MODE = "rgb(25, 36, 52)";

    @Test
    @SuppressWarnings("java:S2925") // suppress warning about Thread.sleep, as this is a test for UI interaction
    void toggleDarkMode() throws InterruptedException {
        final var page = getPage();
        page.navigate(getInstanceUrl());
        page.waitForSelector(INSTANCE_NAME_SELECTOR);
        captureScreenshot("page-before-toggle");

        final var titleColorBeforeToggle = page.evaluate("""
          () => getComputedStyle(document.querySelector('h1'))
              .getPropertyValue('color')
          """).toString();

        page.click(AVATAR_SELECTOR);
        page.waitForSelector(DARK_MODE_MENU_ITEM_SELECTOR);
        captureScreenshot("profile-menu");

        page.click(DARK_MODE_MENU_ITEM_SELECTOR);
        Thread.sleep(100);
        captureScreenshot("page-after-toggle");

        final var titleColorAfterToggle = page.evaluate("""
          () => getComputedStyle(document.querySelector('h1'))
              .getPropertyValue('color')
          """).toString();

        assertThat(titleColorBeforeToggle)
                .isIn(TITLE_COLOR_DARK_MODE, TITLE_COLOR_LIGHT_MODE);
        assertThat(titleColorAfterToggle)
                .isIn(TITLE_COLOR_DARK_MODE, TITLE_COLOR_LIGHT_MODE)
                .isNotSameAs(titleColorBeforeToggle);
    }

}
