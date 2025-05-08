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
package org.komunumo.ui.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.komunumo.data.dto.CommunityDto;
import org.komunumo.data.dto.ImageDto;

public class CommunityCard extends Div {

    private static final String IMAGE_PLACEHOLDER = "/images/placeholder-400x300.jpg";

    public CommunityCard(@NotNull final CommunityDto community,
                         @Nullable final ImageDto image) {
        addClassName("community-card");
        setBackgroundImage(image);
        addOverlay(community);
    }

    private void setBackgroundImage(@Nullable final ImageDto image) {
        final var imageUrl = image == null ? IMAGE_PLACEHOLDER : "/images/" + image.filename();
        getStyle().set("background-image", "url('" + imageUrl + "')");
    }

    private void addOverlay(@NotNull final CommunityDto community) {
        final var overlay = new Div();
        overlay.addClassName("overlay");

        final var title = new H3(community.name());

        overlay.add(title);
        add(overlay);
    }

}
