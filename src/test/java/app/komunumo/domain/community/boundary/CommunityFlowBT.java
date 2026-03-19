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
package app.komunumo.domain.community.boundary;

import app.komunumo.test.BrowserTest;
import com.microsoft.playwright.Page;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class CommunityFlowBT extends BrowserTest {

    protected static final UUID UUID_COMMUNITY = UUID.fromString("9a73690b-6dbd-456a-88e9-dc3f77b69aa0");
    protected static final UUID UUID_OWNER = UUID.fromString("c9fc8b0a-6ff7-4c00-a6f2-d85f5829edff");
    protected static final UUID UUID_MEMBER = UUID.fromString("d56a84c8-e63c-4df4-a854-3af6fb11cdbf");

    protected static final String LEAVE_BUTTON_SELECTOR = "vaadin-button:has-text('Leave Community')";
    protected static final String DEMO_COMMUNITY_NAME_SELECTOR = "h2.community-name";

    protected int getMemberCount(final @NotNull Page page) {
        final var memberCountText = page.locator(".community-memberCount").textContent();
        return Integer.parseInt(memberCountText.replaceAll("\\D+", ""));
    }

}
