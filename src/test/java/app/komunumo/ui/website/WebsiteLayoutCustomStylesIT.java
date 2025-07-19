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
package app.komunumo.ui.website;

import app.komunumo.ui.BrowserTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebsiteLayoutCustomStylesIT extends BrowserTest {

    @Override
    protected String[] getProperties() {
        return new String[] {
                "--komunumo.custom.styles=http://localhost:8082/custom-styles/styles.css"
        };
    }

    @Test
    void testCustomStylesLoadedAndApplied() {
        final var page = getPage();

        page.navigate("http://localhost:8081/");
        page.waitForFunction("""
              () => getComputedStyle(document.documentElement)
                  .getPropertyValue('--komunumo-background-color')
                  .trim() === 'lightblue'
              """);

        captureScreenshot("home-with-custom-styles");

        final var backgroundColor = page.evaluate("""
          () => getComputedStyle(document.querySelector('main'))
              .getPropertyValue('background-color')
          """).toString();

        assertThat(backgroundColor).isEqualTo("rgb(173, 216, 230)"); // lightblue
    }

}
