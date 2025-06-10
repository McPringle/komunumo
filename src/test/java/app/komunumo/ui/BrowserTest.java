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

import app.komunumo.Application;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ScreenshotType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BrowserTest {

    private static final Path SCREENSHOT_DIR = Path.of("target/playwright-screenshots");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserTest.class);
    private static final int PORT = 8081;

    private ConfigurableApplicationContext context;
    private Playwright playwright;
    private Browser browser;
    private Page page;

    private Path screenshotDir;
    private Page.ScreenshotOptions screenshotOptions;

    @BeforeAll
    void startAppAndBrowser() {
        context = SpringApplication.run(Application.class,
                "--server.port=" + PORT, "--spring.profiles.active=test");
        playwright = Playwright.create();
        browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(true));

        screenshotDir = SCREENSHOT_DIR.resolve(getClass().getName()).resolve(browser.browserType().name());
        screenshotOptions = new Page.ScreenshotOptions().setType(ScreenshotType.PNG);
    }

    @AfterAll
    void stopAppAndBrowser() {
        browser.close();
        playwright.close();
        SpringApplication.exit(context);
    }

    @BeforeEach
    void openPage() {
        page = browser.newPage();
    }

    @AfterEach
    void closePage() {
        page.close();
    }

    /**
     * <p>Returns the HTTP port on which the Spring Boot application is currently running.</p>
     *
     * </p>This is useful for constructing URLs in integration tests that perform real HTTP requests
     * against the running application (e.g. API endpoint tests).</p>
     *
     * @return the configured server port, e.g. {@code 8081}
     */
    protected int getPort() {
        return PORT;
    }

    protected Page getPage() {
        return page;
    }

    /**
     * @see ConfigurableApplicationContext#getBean(Class)
     */
    protected <T> T getBean(final @NotNull Class<T> clazz) throws BeansException {
        return context.getBean(clazz);
    }

    /**
     * <p>Captures a screenshot with a given name and prefixed timestamp.</p>
     *
     * @param baseName the base file name (e.g., "home", "login-error")
     */
    protected void captureScreenshot(final @NotNull String baseName) {
        captureScreenshot(baseName, true);
    }

    /**
     * <p>Captures a screenshot with a given name and an optional timestamp prefix.</p>
     *
     * @param baseName         the base file name (e.g., "home", "login-error")
     * @param includeTimestamp if {@code true}, prepends the current timestamp to the file name
     *                         to avoid overwriting previous screenshots (e.g., "20250604-235945342_home.png")
     */
    protected void captureScreenshot(final @NotNull String baseName, final boolean includeTimestamp) {
        try {
            if (!Files.exists(screenshotDir)) {
                Files.createDirectories(screenshotDir);
            }
            final var fileName = includeTimestamp
                    ? TIMESTAMP_FORMAT.format(LocalDateTime.now()) + "_" + baseName + ".png"
                    : baseName + ".png";
            final var path = screenshotDir.resolve(fileName);
            page.screenshot(screenshotOptions.setPath(path));
            LOGGER.info("Screenshot captured and saved to: {}", path.toAbsolutePath());
        } catch (final IOException e) {
            throw new RuntimeException("Failed to capture screenshot", e);
        }
    }

    protected void assertLinkIsVisible(final @NotNull String href) {
        assertThat(page.locator("a[href='%s']".formatted(href)).isVisible()).isTrue();
    }

}
