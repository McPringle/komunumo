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
package app.komunumo.ui.views.admin.importer;

import app.komunumo.data.dto.UserRole;
import app.komunumo.ui.BrowserTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImporterViewBT extends BrowserTest {

    private static final String IMPORTER_SELECTOR =
            "h2:has-text('Import Data')";
    private static final String IMPORTER_MENU_ITEM_SELECTOR =
            "vaadin-context-menu-item[role='menuitem']:has-text('Import Data')";
    private static final String STATUS_TITLE_SELECTOR =
            "h3:has-text('Import Status')";
    private static final String STATUS_LIST_SELECTOR =
            "ul.import-status";
    private static final String IMPORTER_FINISHED_SELECTOR =
            "li:has-text('Import finished')";
    private static final String IMPORTER_FAILED_SELECTOR =
            "li:has-text('Failed to download JSON data from URL')";

    @Test
    void noImportForAnonymousVisitors() {
        final var page = getPage();

        // navigate to events page
        page.navigate(getInstanceUrl() + "events");
        page.waitForURL("**/events");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("anonymousVisitor_eventPageAfterLoad");

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
        captureScreenshot("anonymousVisitor_avatarMenuOpened");

        // check that there is no admin editor menu item
        final var adminItem = page.locator(ADMIN_MENU_ITEM_SELECTOR);
        assertThat(adminItem.isVisible()).isFalse();

        // try to navigate directly to importer will start the authentication process
        page.navigate(getInstanceUrl() + "admin/import");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("anonymousVisitor_afterManualNavigation");
        assertThat(page.locator("h2:visible").allInnerTexts()).contains("Confirm your email address");
    }

    @Test
    void noImportForUserRole() {
        login(getTestUser(UserRole.USER));
        final var page = getPage();

        try {
            // navigate to events page
            page.navigate(getInstanceUrl() + "events");
            page.waitForURL("**/events");
            page.waitForSelector(getInstanceNameSelector());
            captureScreenshot("userRole_eventPageAfterLoad");

            // open avatar menu
            page.click(AVATAR_SELECTOR);
            page.waitForSelector(CONTEXT_MENU_SELECTOR);
            captureScreenshot("userRole_avatarMenuOpened");

            // check that there is no admin editor menu item
            final var adminItem = page.locator(ADMIN_MENU_ITEM_SELECTOR);
            assertThat(adminItem.isVisible()).isFalse();

            // try to navigate directly to importer will show an error message
            page.navigate(getInstanceUrl() + "admin/import");
            page.waitForURL("**/admin/import");
            page.waitForSelector(getInstanceNameSelector());
            captureScreenshot("userRole_manualNavigation");
            assertThat(page.locator("h2:visible").allInnerTexts()).contains("Page not found");
        } finally {
            logout();
        }
    }

    @Test
    void importerWorksWithAdminPermissions() throws InterruptedException {
        login(getTestUser(UserRole.ADMIN));
        final var page = getPage();

        try {
            // navigate to events page
            page.navigate(getInstanceUrl() + "events");
            page.waitForURL("**/events");
            page.waitForSelector(getInstanceNameSelector());
            captureScreenshot("importerWorks_eventPageAfterLoad");

            // open avatar menu
            page.click(AVATAR_SELECTOR);
            page.waitForSelector(CONTEXT_MENU_SELECTOR);
            captureScreenshot("importerWorks_avatarMenuOpened");

            // open admin menu
            final var adminItem = page.locator(ADMIN_MENU_ITEM_SELECTOR);
            adminItem.click();
            captureScreenshot("importerWorks_adminMenuOpened");

            // check that there is an importer menu item
            final var importerItem = page.locator(IMPORTER_MENU_ITEM_SELECTOR);
            assertThat(importerItem.isVisible()).isTrue();

            // click on importer menu item
            importerItem.click();
            page.waitForURL("**/admin/import");
            page.waitForSelector(IMPORTER_SELECTOR);
            captureScreenshot("importerWorks_importerViewLoaded");

            // URL field should be empty
            final var urlField = page.locator("vaadin-text-field.url-field");
            final var urlFieldInput = urlField.locator("input");
            assertThat(urlFieldInput.inputValue()).isEmpty();

            // start import button should be disabled
            final var startImportButton = page.locator("vaadin-button.start-import-button");
            assertThat(startImportButton.isEnabled()).isFalse();

            // status infos should be hidden
            final var statusTitle = page.locator(STATUS_TITLE_SELECTOR);
            assertThat(statusTitle.isVisible()).isFalse();
            final var statusList = page.locator(STATUS_LIST_SELECTOR);
            assertThat(statusList.isVisible()).isFalse();
            assertThat(statusList.locator("li").count()).isZero();

            // fill in URL field
            urlFieldInput.fill("http://localhost:8082/import/data.json");
            Thread.sleep(100); // wait for all UI animations to finish

            // start import button should be enabled
            assertThat(startImportButton.isEnabled()).isTrue();

            // click start import button
            startImportButton.click();
            Thread.sleep(100); // wait for all UI animations to finish

            // URL field and button should be disabled during import
            assertThat(urlFieldInput.isEnabled()).isFalse();
            assertThat(startImportButton.isEnabled()).isFalse();

            // status infos should be visible
            assertThat(statusTitle.isVisible()).isTrue();
            assertThat(statusList.isVisible()).isTrue();
            assertThat(statusList.locator("li").count()).isGreaterThan(0);

            // wait for import to finish
            page.waitForSelector(IMPORTER_FINISHED_SELECTOR);
            Thread.sleep(100); // wait for all UI animations to finish
            captureScreenshot("importerWorks_importerFinished");

            // URL field and button should be enabled again after import
            assertThat(urlFieldInput.isEnabled()).isTrue();
            assertThat(startImportButton.isEnabled()).isTrue();

            // empty URL field again
            urlFieldInput.fill("");
            Thread.sleep(100); // wait for all UI animations to finish

            // start import button should be disabled again
            assertThat(startImportButton.isEnabled()).isFalse();

        } finally {
            logout();
        }
    }

    @Test
    void importerFailsWithIncorrectURL() throws InterruptedException {
        login(getTestUser(UserRole.ADMIN));
        final var page = getPage();

        try {
            // navigate to events page
            page.navigate(getInstanceUrl() + "events");
            page.waitForURL("**/events");
            page.waitForSelector(getInstanceNameSelector());
            captureScreenshot("incorrectURL_eventPageAfterLoad");

            // open avatar menu
            page.click(AVATAR_SELECTOR);
            page.waitForSelector(CONTEXT_MENU_SELECTOR);
            captureScreenshot("incorrectURL_avatarMenuOpened");

            // open admin menu
            final var adminItem = page.locator(ADMIN_MENU_ITEM_SELECTOR);
            adminItem.click();
            captureScreenshot("incorrectURL_adminMenuOpened");

            // check that there is an importer menu item
            final var importerItem = page.locator(IMPORTER_MENU_ITEM_SELECTOR);
            assertThat(importerItem.isVisible()).isTrue();

            // click on importer menu item
            importerItem.click();
            page.waitForURL("**/admin/import");
            page.waitForSelector(IMPORTER_SELECTOR);
            captureScreenshot("incorrectURL_importerViewLoaded");

            // fill in URL field
            final var urlField = page.locator("vaadin-text-field.url-field");
            final var urlFieldInput = urlField.locator("input");
            urlFieldInput.fill("http://localhost:8082/import/non-existing.json");
            Thread.sleep(100); // wait for all UI animations to finish

            // start import button should be enabled
            final var startImportButton = page.locator("vaadin-button.start-import-button");
            assertThat(startImportButton.isEnabled()).isTrue();

            // click start import button
            startImportButton.click();
            Thread.sleep(100); // wait for all UI animations to finish

            // wait for import to fail
            page.waitForSelector(IMPORTER_FAILED_SELECTOR);
            Thread.sleep(100); // wait for all UI animations to finish
            captureScreenshot("incorrectURL_importerFailed");

            // check status infos
            final var statusTitle = page.locator(STATUS_TITLE_SELECTOR);
            assertThat(statusTitle.isVisible()).isTrue();
            final var statusList = page.locator(STATUS_LIST_SELECTOR);
            assertThat(statusList.isVisible()).isTrue();
            assertThat(statusList.locator("li").count()).isGreaterThan(0);

        } finally {
            logout();
        }
    }
}
