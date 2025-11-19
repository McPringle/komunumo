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
package app.komunumo.ui.views;

import app.komunumo.business.core.config.control.ConfigurationService;
import app.komunumo.ui.BrowserTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import static app.komunumo.business.core.config.entity.ConfigurationSetting.INSTANCE_CUSTOM_STYLES;

class WebsiteLayoutCustomStylesBT extends BrowserTest {

    @Autowired
    private @NotNull ConfigurationService configurationService;

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:8082/custom-styles/styles.css",
            "http://localhost:8082/custom-styles/styles.css?foo=bar"
    })
    @SuppressWarnings("java:S2699")
    void testCustomStyles(final @NotNull String customStylesUrl) {
        try {
            configurationService.setConfiguration(INSTANCE_CUSTOM_STYLES, customStylesUrl);

            final var page = getPage();
            page.navigate(getInstanceUrl());
            page.waitForFunction("""
                    () => getComputedStyle(document.body, '::after')
                        .getPropertyValue('content')
                        .includes('Custom styles applied!')
                    """);
            captureScreenshot("home-with-custom-styles");
        } finally {
            configurationService.setConfiguration(INSTANCE_CUSTOM_STYLES, "");
        }
    }

}
