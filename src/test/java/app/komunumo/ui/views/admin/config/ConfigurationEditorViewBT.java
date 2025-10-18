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
package app.komunumo.ui.views.admin.config;

import app.komunumo.data.dto.UserRole;
import app.komunumo.ui.BrowserTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationEditorViewBT extends BrowserTest {

    private static final String CONFIGURATION_EDITOR_SELECTOR =
            "h2:has-text('Edit Configuration')";
    private static final String CONFIGURATION_EDITOR_MENU_ITEM_SELECTOR =
            "vaadin-context-menu-item[role='menuitem']:has-text('Edit Configuration')";

    @Test
    void noConfigurationEditorForAnonymousVisitors() {
        final var page = getPage();

        // navigate to events page
        page.navigate(getInstanceUrl() + "events");
        page.waitForURL("**/events");
        page.waitForSelector(INSTANCE_NAME_SELECTOR);
        captureScreenshot("anonymousVisitor_eventPageAfterLoad");

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
        captureScreenshot("anonymousVisitor_avatarMenuOpened");

        // check that there is no configuration editor menu item
        final var configItem = page.locator(CONFIGURATION_EDITOR_MENU_ITEM_SELECTOR);
        assertThat(configItem.isVisible()).isFalse();

        // try to navigate directly to configuration editor will start the authentication process
        page.navigate(getInstanceUrl() + "admin/config");
        page.waitForSelector(INSTANCE_NAME_SELECTOR);
        captureScreenshot("anonymousVisitor_afterManualNavigation");
        assertThat(page.locator("h2:visible").allInnerTexts()).contains("Confirm your email address");
    }

    @Test
    void noConfigurationEditorForUserRole() {
        login(getTestUser(UserRole.USER));
        final var page = getPage();

        try {
            // navigate to events page
            page.navigate(getInstanceUrl() + "events");
            page.waitForURL("**/events");
            page.waitForSelector(INSTANCE_NAME_SELECTOR);
            captureScreenshot("userRole_eventPageAfterLoad");

            // open avatar menu
            page.click(AVATAR_SELECTOR);
            page.waitForSelector(CONTEXT_MENU_SELECTOR);
            captureScreenshot("userRole_avatarMenuOpened");

            // check that there is no configuration editor menu item
            final var configItem = page.locator(CONFIGURATION_EDITOR_MENU_ITEM_SELECTOR);
            assertThat(configItem.isVisible()).isFalse();

            // try to navigate directly to configuration editor will show an error message
            page.navigate(getInstanceUrl() + "admin/config");
            page.waitForURL("**/admin/config");
            page.waitForSelector(INSTANCE_NAME_SELECTOR);
            captureScreenshot("userRole_manualNavigation");
            assertThat(page.locator("h2:visible").allInnerTexts()).contains("Page not found");
        } finally {
            logout();
        }
    }

    @Test
    void configurationEditorShownForUserAdmin() {
        login(getTestUser(UserRole.ADMIN));
        final var page = getPage();

        try {
            // navigate to events page
            page.navigate(getInstanceUrl() + "events");
            page.waitForURL("**/events");
            page.waitForSelector(INSTANCE_NAME_SELECTOR);
            captureScreenshot("adminRole_eventPageAfterLoad");

            // open avatar menu
            page.click(AVATAR_SELECTOR);
            page.waitForSelector(CONTEXT_MENU_SELECTOR);
            captureScreenshot("adminRole_avatarMenuOpened");

            // check that there is a configuration editor menu item
            final var configItem = page.locator(CONFIGURATION_EDITOR_MENU_ITEM_SELECTOR);
            assertThat(configItem.isVisible()).isTrue();

            // click on configuration editor menu item
            configItem.click();
            page.waitForURL("**/admin/config");
            page.waitForSelector(INSTANCE_NAME_SELECTOR);
            captureScreenshot("adminRole_configurationEditor");
            assertThat(page.locator("h2:visible").allInnerTexts()).contains("Edit Configuration");
        } finally {
            logout();
        }
    }

    @Test
    void configurationEditorFlow() {
        login(getTestUser(UserRole.ADMIN));
        final var instanceNameSelector = "header.page-header h1";
        final var page = getPage();

        try {
            // navigate to configuration editor
            page.navigate(getInstanceUrl() + "admin/config");
            page.waitForURL("**/admin/config");
            page.waitForSelector(CONFIGURATION_EDITOR_SELECTOR);
            captureScreenshot("flow_configurationEditor_afterLoad");

            // instance name should be the default one
            assertThat(page.locator(instanceNameSelector).innerText()).isEqualTo("Your Instance Name");

            // instance name should be empty in the input field
            final var instanceNameSetting = page.locator(".setting-instance-name ");
            final var instanceNameField = instanceNameSetting.locator("vaadin-text-field");
            final var instanceNameInput = instanceNameField.locator("input");
            assertThat(instanceNameInput.inputValue()).isEmpty();

            // instance name buttons should be all disabled
            final var instanceNameDefaultButton = instanceNameSetting.locator("vaadin-button.default-button");
            final var instanceNameResetButton = instanceNameSetting.locator("vaadin-button.reset-button");
            final var instanceNameSaveButton = instanceNameSetting.locator("vaadin-button.save-button");
            assertThat(instanceNameDefaultButton.isEnabled()).isFalse();
            assertThat(instanceNameResetButton.isEnabled()).isFalse();
            assertThat(instanceNameSaveButton.isEnabled()).isFalse();

            // edit instance name
            instanceNameInput.fill("New Instance Name");
            captureScreenshot("flow_configurationEditor_afterEditInstanceName");
            assertThat(instanceNameDefaultButton.isEnabled()).isTrue();
            assertThat(instanceNameResetButton.isEnabled()).isFalse();
            assertThat(instanceNameSaveButton.isEnabled()).isTrue();

            // save instance name
            instanceNameSaveButton.click();
            captureScreenshot("flow_configurationEditor_afterSaveInstanceName");
            assertThat(instanceNameDefaultButton.isEnabled()).isTrue();
            assertThat(instanceNameResetButton.isEnabled()).isFalse();
            assertThat(instanceNameSaveButton.isEnabled()).isFalse();

            // reload to make changes visible
            page.reload();
            page.waitForURL("**/admin/config");
            page.waitForSelector(CONFIGURATION_EDITOR_SELECTOR);
            captureScreenshot("flow_configurationEditor_afterReload");

            // instance name should be modified
            assertThat(page.locator(instanceNameSelector).innerText()).isEqualTo("New Instance Name");

            // instance name should be set in the input field
            assertThat(instanceNameInput.inputValue()).isEqualTo("New Instance Name");
            assertThat(instanceNameDefaultButton.isEnabled()).isTrue();
            assertThat(instanceNameResetButton.isEnabled()).isFalse();
            assertThat(instanceNameSaveButton.isEnabled()).isFalse();

            // edit instance name again
            instanceNameInput.fill("Another Instance Name");
            captureScreenshot("flow_configurationEditor_afterEditInstanceNameAgain");
            assertThat(instanceNameDefaultButton.isEnabled()).isTrue();
            assertThat(instanceNameResetButton.isEnabled()).isTrue();
            assertThat(instanceNameSaveButton.isEnabled()).isTrue();

            // Reset to stored value
            instanceNameResetButton.click();
            captureScreenshot("flow_configurationEditor_afterResetInstanceName");
            assertThat(instanceNameInput.inputValue()).isEqualTo("New Instance Name");
            assertThat(instanceNameDefaultButton.isEnabled()).isTrue();
            assertThat(instanceNameResetButton.isEnabled()).isFalse();
            assertThat(instanceNameSaveButton.isEnabled()).isFalse();

            // Reset to default value
            instanceNameDefaultButton.click();
            captureScreenshot("flow_configurationEditor_afterDefaultInstanceName");
            assertThat(instanceNameInput.inputValue()).isEqualTo("Your Instance Name");
            assertThat(instanceNameDefaultButton.isEnabled()).isFalse();
            assertThat(instanceNameResetButton.isEnabled()).isTrue();
            assertThat(instanceNameSaveButton.isEnabled()).isTrue();

            // Save default value
            instanceNameSaveButton.click();
            captureScreenshot("flow_configurationEditor_afterSaveDefaultInstanceName");
            assertThat(instanceNameInput.inputValue()).isEqualTo("Your Instance Name");
            assertThat(instanceNameDefaultButton.isEnabled()).isFalse();
            assertThat(instanceNameResetButton.isEnabled()).isFalse();
            assertThat(instanceNameSaveButton.isEnabled()).isFalse();

            // reload to make changes visible
            page.reload();
            page.waitForURL("**/admin/config");
            page.waitForSelector(CONFIGURATION_EDITOR_SELECTOR);
            captureScreenshot("flow_configurationEditor_afterSecondReload");

            // instance name should be the default again
            assertThat(page.locator(instanceNameSelector).innerText()).isEqualTo("Your Instance Name");

            // instance name should be empty in the input field
            assertThat(instanceNameInput.inputValue()).isEmpty();
            assertThat(instanceNameDefaultButton.isEnabled()).isFalse();
            assertThat(instanceNameResetButton.isEnabled()).isFalse();
            assertThat(instanceNameSaveButton.isEnabled()).isFalse();
        } finally {
            logout();
        }
    }

}
