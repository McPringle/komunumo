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

import app.komunumo.ui.BrowserTest;
import org.junit.jupiter.api.Test;

class WebsiteLayoutCustomStylesParameterIT extends BrowserTest {

    @Override
    protected String[] getProperties() {
        return new String[] {
                "--komunumo.instance.styles=http://localhost:8082/custom-styles/styles.css?foo=bar"
        };
    }

    @Test
    @SuppressWarnings("java:S2699")
    void testCustomStylesLoadedAndApplied() {
        final var page = getPage();
        page.navigate("http://localhost:8081/");
        page.waitForFunction("""
            () => getComputedStyle(document.body, '::after')
                .getPropertyValue('content')
                .includes('Custom styles applied!')
            """);
        captureScreenshot("home-with-custom-styles");
    }

}
