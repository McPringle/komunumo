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

import app.komunumo.domain.core.config.entity.AppConfig;
import app.komunumo.test.BrowserTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.assertj.core.api.Assertions.assertThat;

class WebsiteLayoutCustomStylesBT extends BrowserTest {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    void testWithoutCustomStyles() {
        final var page = getPage();
        page.navigate(getInstanceUrl());

        final var locators = page.locator("link[rel='stylesheet']")
                .all()
                .stream()
                .map(locator -> locator.getAttribute("href"))
                .toList();

        assertThat(locators).doesNotContain("/custom/styles/styles.css");
    }

    @Test
    void testWithCustomStyles() throws IOException {
        final var stylesFile = createTestCssFile();

        try {
            final var page = getPage();
            page.navigate(getInstanceUrl());

            final var locators = page.locator("link[rel='stylesheet']")
                    .all()
                    .stream()
                    .map(locator -> locator.getAttribute("href"))
                    .toList();

            assertThat(locators).contains("/custom/styles/styles.css");
        } finally {
            Files.deleteIfExists(stylesFile);
        }
    }

    @Test
    void testWithCustomStylesApplied() throws IOException {
        final var stylesFile = createTestCssFile();

        try {
            final var page = getPage();
            page.navigate(getInstanceUrl());
            page.waitForFunction("""
                    () => getComputedStyle(document.body, '::after')
                        .getPropertyValue('content')
                        .includes('Custom styles applied!')
                    """);
            captureScreenshot("home-with-custom-styles");
        } finally {
            Files.deleteIfExists(stylesFile);
        }
    }

    private @NonNull Path createTestCssFile() throws IOException {
        final var cssContent = resourceLoader.getResource("classpath:custom-styles/styles.css")
                .getContentAsString(StandardCharsets.UTF_8);
        final var stylesDirectory = appConfig.files().basedir().resolve("custom", "styles");
        Files.createDirectories(stylesDirectory);
        final var stylesFile = stylesDirectory.resolve("styles.css");
        Files.writeString(stylesFile, cssContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        return stylesFile;
    }

}
