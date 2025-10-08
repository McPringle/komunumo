package app.komunumo.ui.views.admin.config;

import app.komunumo.data.dto.UserRole;
import app.komunumo.ui.BrowserTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationEditorViewIT extends BrowserTest {

    private static final String CONFIGURATION_EDITOR_MENU_ITEM_SELECTOR =
            "vaadin-context-menu-item[role='menuitem']:has-text('Edit Configuration')";

    @Test
    void noConfigurationEditorForAnonymousVisitors() {
        final var page = getPage();

        // navigate to events page
        page.navigate("http://localhost:8081/events");
        page.waitForURL("**/events");
        page.waitForSelector(INSTANCE_NAME_SELECTOR);
        captureScreenshot("anonymousVisitor_eventPageAfterload");

        // open avatar menu
        page.click(AVATAR_SELECTOR);
        page.waitForSelector(LOGIN_MENU_ITEM_SELECTOR);
        captureScreenshot("anonymousVisitor_avatarMenuOpened");

        // check that there is no configuration editor menu item
        final var configItem = page.locator(CONFIGURATION_EDITOR_MENU_ITEM_SELECTOR);
        assertThat(configItem.isVisible()).isFalse();

        // try to navigate directly to configuration editor will start the authentication process
        page.navigate("http://localhost:8081/admin/config");
        page.waitForURL("**/admin/config");
        page.waitForSelector(INSTANCE_NAME_SELECTOR);
        final var overlay = page.locator("vaadin-dialog-overlay[opened]")
                .filter(new Locator.FilterOptions().setHas(page.locator("vaadin-email-field")));
        overlay.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.waitForFunction("overlay => !overlay.hasAttribute('opening')", overlay.elementHandle());
        captureScreenshot("anonymousVisitor_manualNavigation");
    }

    @Test
    void noConfigurationEditorForUserRole() {
        login(getTestUser(UserRole.USER));
        final var page = getPage();

        try {
            // navigate to events page
            page.navigate("http://localhost:8081/events");
            page.waitForURL("**/events");
            page.waitForSelector(INSTANCE_NAME_SELECTOR);
            captureScreenshot("userRole_eventPageAfterload");

            // open avatar menu
            page.click(AVATAR_SELECTOR);
            page.waitForSelector(LOGOUT_MENU_ITEM_SELECTOR);
            captureScreenshot("userRole_avatarMenuOpened");

            // check that there is no configuration editor menu item
            final var configItem = page.locator(CONFIGURATION_EDITOR_MENU_ITEM_SELECTOR);
            assertThat(configItem.isVisible()).isFalse();

            // try to navigate directly to configuration editor will show an error message
            page.navigate("http://localhost:8081/admin/config");
            page.waitForURL("**/admin/config");
            page.waitForSelector(INSTANCE_NAME_SELECTOR);
            captureScreenshot("userRole_manualNavigation");
            assertThat(page.locator("h2").allInnerTexts()).contains("Page not found");
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
            page.navigate("http://localhost:8081/events");
            page.waitForURL("**/events");
            page.waitForSelector(INSTANCE_NAME_SELECTOR);
            captureScreenshot("adminRole_eventPageAfterload");

            // open avatar menu
            page.click(AVATAR_SELECTOR);
            page.waitForSelector(LOGOUT_MENU_ITEM_SELECTOR);
            captureScreenshot("adminRole_avatarMenuOpened");

            // check that there is a configuration editor menu item
            final var configItem = page.locator(CONFIGURATION_EDITOR_MENU_ITEM_SELECTOR);
            assertThat(configItem.isVisible()).isTrue();

            // click on configuration editor menu item
            configItem.click();
            page.waitForURL("**/admin/config");
            page.waitForSelector(INSTANCE_NAME_SELECTOR);
            captureScreenshot("adminRole_configurationEditor");
            assertThat(page.locator("h2").allInnerTexts()).contains("Edit Configuration");
        } finally {
            logout();
        }
    }

}
