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
package app.komunumo.domain.core.exporter.boundary;

import app.komunumo.domain.user.entity.UserRole;
import app.komunumo.test.BrowserTest;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static app.komunumo.data.db.tables.Image.IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.SQLDataType.VARCHAR;

class ExporterViewBT extends BrowserTest {

    private static final String EXPORTER_SELECTOR =
            "h2:has-text('Export Data')";
    private static final String EXPORTER_MENU_ITEM_SELECTOR =
            "vaadin-context-menu-item[role='menuitem']:has-text('Export Data')";

    @TempDir
    private Path tempDir;

    @Autowired
    private DSLContext dsl;

    @Test
    void noExportForAnonymousVisitors() {
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

        // try to navigate directly to exporter will start the authentication process
        page.navigate(getInstanceUrl() + "admin/export");
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("anonymousVisitor_afterManualNavigation");
        assertThat(page.locator("h2:visible").allInnerTexts()).contains("Confirm your email address");
    }

    @Test
    void noExportForUserRole() {
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

            // try to navigate directly to exporter will show an error message
            page.navigate(getInstanceUrl() + "admin/export");
            page.waitForURL("**/admin/export");
            page.waitForSelector(getInstanceNameSelector());
            captureScreenshot("userRole_manualNavigation");
            assertThat(page.locator("h2:visible").allInnerTexts()).contains("Page not found");
        } finally {
            logout();
        }
    }

    @Test
    void exporterWorksAndDownloads() throws IOException {
        login(getTestUser(UserRole.ADMIN));
        final var page = getPage();

        try {
            // navigate to events page
            page.navigate(getInstanceUrl() + "events");
            page.waitForURL("**/events");
            page.waitForSelector(getInstanceNameSelector());
            captureScreenshot("exporterWorksAndDownloads_eventPageAfterLoad");

            // open avatar menu
            page.click(AVATAR_SELECTOR);
            page.waitForSelector(CONTEXT_MENU_SELECTOR);
            captureScreenshot("exporterWorksAndDownloads_avatarMenuOpened");

            // open admin menu
            final var adminItem = page.locator(ADMIN_MENU_ITEM_SELECTOR);
            adminItem.click();
            captureScreenshot("exporterWorksAndDownloads_adminMenuOpened");

            // check that there is an exporter menu item
            final var exporterItem = page.locator(EXPORTER_MENU_ITEM_SELECTOR);
            assertThat(exporterItem.isVisible()).isTrue();

            // click on exporter menu item
            exporterItem.click();
            page.waitForURL("**/admin/export");
            page.waitForSelector(EXPORTER_SELECTOR);
            captureScreenshot("exporterWorksAndDownloads_exporterViewLoaded");

            // start export button should be enabled
            final var startExportButton = page.locator("vaadin-button.start-export-button");
            assertThat(startExportButton.isEnabled()).isTrue();

            // start the export and wait for it to finish
            startExportButton.click();
            page.waitForSelector("li:has-text('Export started')");
            page.waitForSelector("li:has-text('Export successful')");

            // Start the download
            final var download = page.waitForDownload(() -> {
                final var downloadLink = page.locator("a.export-download-link");
                downloadLink.click();
            });

            // check that the suggested filename is correct
            final var fileName = download.suggestedFilename();
            assertThat(fileName).startsWith("komunumo-export-");
            assertThat(fileName).endsWith(".json");

            // wait for the download process to complete and save the downloaded file somewhere
            final var tempFile = tempDir.resolve(fileName);
            download.saveAs(tempFile);

            // check downloaded file
            assertThat(Files.exists(tempFile)).isTrue();
            assertThat(Files.size(tempFile)).isGreaterThan(10 * 1024); // 10 KB
        } finally {
            logout();
        }
    }

    @Test
    void exporterFailsWithError() {
        final Field<String> CONTENT_TYPE_RAW =
                field(IMAGE.CONTENT_TYPE.getQualifiedName(), VARCHAR);
        dsl.insertInto(IMAGE)
                .set(IMAGE.ID, UUID.randomUUID())
                .set(CONTENT_TYPE_RAW, "invalid/content-type")
                .execute();

        login(getTestUser(UserRole.ADMIN));
        final var page = getPage();

        try {
            // navigate to events page
            page.navigate(getInstanceUrl() + "events");
            page.waitForURL("**/events");
            page.waitForSelector(getInstanceNameSelector());
            captureScreenshot("exporterFailsWithError_eventPageAfterLoad");

            // open avatar menu
            page.click(AVATAR_SELECTOR);
            page.waitForSelector(CONTEXT_MENU_SELECTOR);
            captureScreenshot("exporterFailsWithError_avatarMenuOpened");

            // open admin menu
            final var adminItem = page.locator(ADMIN_MENU_ITEM_SELECTOR);
            adminItem.click();
            captureScreenshot("exporterFailsWithError_adminMenuOpened");

            // check that there is an exporter menu item
            final var exporterItem = page.locator(EXPORTER_MENU_ITEM_SELECTOR);
            assertThat(exporterItem.isVisible()).isTrue();

            // click on exporter menu item
            exporterItem.click();
            page.waitForURL("**/admin/export");
            page.waitForSelector(EXPORTER_SELECTOR);
            captureScreenshot("exporterFailsWithError_exporterViewLoaded");

            // start export button should be enabled
            final var startExportButton = page.locator("vaadin-button.start-export-button");
            assertThat(startExportButton.isEnabled()).isTrue();

            // start the export and wait for the error
            startExportButton.click();
            page.waitForSelector("li:has-text('Export started')");
            page.waitForSelector("li:has-text('Export failed')");
            captureScreenshot("exporterFailsWithError_errorShown");
        } finally {
            logout();
        }
    }

}
