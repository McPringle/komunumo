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
package app.komunumo.ui;

import app.komunumo.data.dto.ConfigurationSetting;
import app.komunumo.data.dto.UserDto;
import app.komunumo.data.dto.UserRole;
import app.komunumo.data.dto.UserType;
import app.komunumo.data.service.ConfigurationService;
import app.komunumo.data.service.UserService;
import app.komunumo.security.SystemAuthenticator;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.ScreenshotType;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static app.komunumo.util.TestUtil.extractLinkFromText;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;

public abstract class BrowserTest extends IntegrationTest {

    protected static final String INSTANCE_NAME_SELECTOR = "h1:has-text('Your Instance Name')";
    protected static final String AVATAR_SELECTOR = "vaadin-avatar";
    protected static final String LOGIN_MENU_ITEM_SELECTOR = "vaadin-context-menu-item[role='menuitem']:has-text('Login')";
    protected static final String LOGOUT_MENU_ITEM_SELECTOR = "vaadin-context-menu-item[role='menuitem']:has-text('Logout')";

    private static final Path SCREENSHOT_DIR = Path.of("target/playwright-screenshots");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserTest.class);

    private static Playwright playwright;
    private static Browser browser;
    private Page page;

    private Path screenshotDir;
    private Page.ScreenshotOptions screenshotOptions;

    private String instanceUrl = "";

    @BeforeAll
    static void startAppAndBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void stopAppAndBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void openPage() {
        screenshotDir = SCREENSHOT_DIR.resolve(getClass().getName()).resolve(browser.browserType().name());
        screenshotOptions = new Page.ScreenshotOptions()
                .setType(ScreenshotType.PNG)
                .setFullPage(true);

        instanceUrl = "http://localhost:%d/".formatted(getPort());
        final var configurationService = getBean(ConfigurationService.class);
        final var systemAuthenticator = getBean(SystemAuthenticator.class);
        systemAuthenticator.runAsAdmin(
                () -> configurationService.setConfiguration(ConfigurationSetting.INSTANCE_URL, instanceUrl));

        final var pageOptions = new Browser.NewPageOptions()
                .setViewportSize(1920, 1080);
        page = browser.newPage(pageOptions);
    }

    @AfterEach
    void closePage() {
        page.close();
    }

    protected String getInstanceUrl() {
        return instanceUrl;
    }

    /**
     * <p>Returns the current Playwright {@link Page} instance used in the test.</p>
     *
     * <p>This page is created before each test and closed afterward.
     * It can be used to interact with the browser, navigate to URLs,
     * or assert the presence of elements on the page.</p>
     *
     * @return the current {@link Page} instance
     */
    protected Page getPage() {
        return page;
    }

    /**
     * <p>Captures a screenshot with a given name and prefixed timestamp.</p>
     *
     * @param baseName the base file name (e.g., "home", "login-error")
     */
    protected void captureScreenshot(final @NotNull String baseName) {
        try {
            if (!Files.exists(screenshotDir)) {
                Files.createDirectories(screenshotDir);
            }
            final var fileName = TIMESTAMP_FORMAT.format(LocalDateTime.now()) + "_" + baseName + ".png";
            final var path = screenshotDir.resolve(fileName);
            page.screenshot(screenshotOptions.setPath(path));
            LOGGER.info("Screenshot captured and saved to: {}", path.toAbsolutePath());
        } catch (final IOException e) {
            throw new RuntimeException("Failed to capture screenshot", e);
        }
    }

    /**
     * Returns the predefined test user for the given role.
     *
     * <p>The UUIDs of the test users are fixed in {@link TestConstants} and
     * the corresponding records are inserted into the database by Flyway
     * test migrations. This method provides a convenient way for integration
     * tests to access those users without duplicating IDs.</p>
     *
     * @param role the {@link UserRole} to select
     * @return the {@link UserDto} for the requested role
     * @throws IllegalStateException if the user could not be found in the database
     */
    protected @NotNull UserDto getTestUser(final @NotNull UserRole role) {
        final var userService = getBean(UserService.class);
        return switch (role) {
            case USER -> userService.getUserById(TestConstants.USER_ID_LOCAL)
                    .orElseThrow(() -> new IllegalStateException("Test USER not found in database"));
            case ADMIN -> userService.getUserById(TestConstants.USER_ID_ADMIN)
                    .orElseThrow(() -> new IllegalStateException("Test ADMIN not found in database"));
        };
    }

    /**
     * Returns the predefined test user for the given type.
     *
     * <p>The UUIDs of the test users are fixed in {@link TestConstants} and
     * the corresponding records are inserted into the database by Flyway
     * test migrations. This method provides a convenient way for integration
     * tests to access those users without duplicating IDs.</p>
     *
     * @param type the {@link UserType} to select
     * @return the {@link UserDto} for the requested type
     * @throws IllegalStateException if the user could not be found in the database
     */
    protected @NotNull UserDto getTestUser(final @NotNull UserType type) {
        final var userService = getBean(UserService.class);
        return switch (type) {
            case LOCAL -> userService.getUserById(TestConstants.USER_ID_LOCAL)
                    .orElseThrow(() -> new IllegalStateException("LOCAL user not found in database"));
            case REMOTE -> userService.getUserById(TestConstants.USER_ID_REMOTE)
                    .orElseThrow(() -> new IllegalStateException("REMOTE user not found in database"));
            case ANONYMOUS -> userService.getUserById(TestConstants.USER_ID_ANONYMOUS)
                    .orElseThrow(() -> new IllegalStateException("ANONYMOUS user not found in database"));
        };
    }

    /**
     * Logs in a user for browser tests.
     *
     * @param user the user to log in
     */
    protected void login(final @NotNull UserDto user) {
        final var greenMail = getGreenMail();

        // navigate to login page
        page.navigate("http://localhost:%d/login".formatted(getPort()));
        page.waitForURL("**/login");
        page.waitForSelector(INSTANCE_NAME_SELECTOR);

        // wait for login dialog to appear
        final var overlay = page.locator("vaadin-dialog-overlay[opened]")
                .filter(new Locator.FilterOptions().setHas(page.locator("vaadin-email-field")));
        overlay.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.waitForFunction("overlay => !overlay.hasAttribute('opening')", overlay.elementHandle());
        captureScreenshot("login_empty-dialog");

        // fill in email address
        final var emailInput = page.locator("vaadin-email-field").locator("input");
        emailInput.fill(user.email());
        captureScreenshot("login_email-field-set");

        // click on the request email button
        final var mailCount = greenMail.getReceivedMessages().length;
        page.locator("vaadin-button.email-button").click();

        // close the dialog
        final var closeButton = page.locator("vaadin-button:has-text(\"Close\")");
        closeButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        captureScreenshot("login_email-send");
        closeButton.click();

        // wait for the confirmation email
        await().atMost(2, SECONDS).untilAsserted(() -> greenMail.waitForIncomingEmail(mailCount + 1));
        final var receivedMessage = greenMail.getReceivedMessages()[0];
        assertThatCode(() ->
                assertThat(receivedMessage.getSubject())
                        .isEqualTo("[Your Instance Name] Please confirm your email address")
        ).doesNotThrowAnyException();

        // extract the confirmation link
        final var mailBody = GreenMailUtil.getBody(receivedMessage);
        final var confirmationLink = extractLinkFromText(mailBody);
        assertThat(confirmationLink).isNotNull();

        // open the confirmation link
        page.navigate(confirmationLink);
        page.waitForURL("**/confirm**");
        page.waitForSelector(INSTANCE_NAME_SELECTOR);
        captureScreenshot("login_confirmation-page");
    }

    protected void logout() {
        if (page.locator(LOGOUT_MENU_ITEM_SELECTOR).count() == 0) {
            page.click(AVATAR_SELECTOR);
            page.waitForSelector(LOGOUT_MENU_ITEM_SELECTOR);
        }
        captureScreenshot("logout_profile-menu");

        page.click(LOGOUT_MENU_ITEM_SELECTOR);
        page.waitForSelector("vaadin-context-menu-overlay",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(1500));
        } catch (PlaywrightException ignored) { }
        captureScreenshot("logout_done");
    }

}
