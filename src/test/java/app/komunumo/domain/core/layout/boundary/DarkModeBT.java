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

import app.komunumo.test.BrowserTest;
import com.microsoft.playwright.Page;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DarkModeBT extends BrowserTest {

    protected static final String AVATAR_SELECTOR = "vaadin-avatar";
    protected static final String DARK_MODE_MENU_ITEM_SELECTOR = "vaadin-context-menu-item[role='menuitem']:has-text('Toggle Dark Mode')";
    protected static final String TITLE_COLOR_DARK_MODE = "oklch(0.15 0.0038 248)";
    protected static final String TITLE_COLOR_LIGHT_MODE = "oklch(1 0.002 260)";

    @Test
    @SuppressWarnings("java:S2925") // suppress warning about Thread.sleep, as this is a test for UI interaction
    void toggleDarkMode() throws InterruptedException {
        final var page = getPage();
        page.navigate(getInstanceUrl());
        page.waitForSelector(getInstanceNameSelector());
        captureScreenshot("page-before-toggle");

        final var titleColorBeforeToggle = computedColorAsRgb(page);

        page.click(AVATAR_SELECTOR);
        page.waitForSelector(CONTEXT_MENU_SELECTOR);
        captureScreenshot("profile-menu");

        page.click(DARK_MODE_MENU_ITEM_SELECTOR);
        page.waitForFunction("""
            (args) => {
              const el = document.querySelector('h1');
              if (!el) return false;

              const color = getComputedStyle(el).getPropertyValue('color').trim();
              const canvas = document.createElement('canvas');
              const ctx = canvas.getContext('2d');
              ctx.fillStyle = '#000';
              ctx.fillStyle = color;
              return ctx.fillStyle !== args.before;
            }
            """, Map.of("before", titleColorBeforeToggle));
        captureScreenshot("page-after-toggle");

        final var titleColorAfterToggle = computedColorAsRgb(page);

        assertThat(titleColorAfterToggle).isNotEqualTo(titleColorBeforeToggle);
    }

    private static String computedColorAsRgb(final @NotNull Page page) {
        return page.evaluate("""
        (args) => {
          const el = document.querySelector('h1');
          if (!el) return null;
          const color = getComputedStyle(el).getPropertyValue('color').trim();

          // Normalize to rgb/rgba via canvas
          const canvas = document.createElement('canvas');
          const ctx = canvas.getContext('2d');
          ctx.fillStyle = '#000';
          ctx.fillStyle = color;
          return ctx.fillStyle; // typically "rgb(r, g, b)" or "rgba(...)"
        }
        """).toString();
    }

}
