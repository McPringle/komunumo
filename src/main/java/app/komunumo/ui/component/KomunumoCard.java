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
package app.komunumo.ui.component;

import app.komunumo.data.dto.ImageDto;
import app.komunumo.util.ImageUtil;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.html.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>Abstract base class for cards used in Komunumo's UI, based on Vaadin's {@link Card} component.</p>
 *
 * <p>Adds common styling and support for a title and optional media content (image).</p>
 */
public abstract class KomunumoCard extends Card {

    /**
     * <p>Creates a new {@code KomunumoCard} with default styling and no content.</p>
     *
     * <p>Applies the CSS class {@code komunumo-card} and the Lumo theme variants:
     * {@code outlined}, {@code elevated}, and {@code cover-media}.</p>
     */
    protected KomunumoCard() {
        super();
        addClassName("komunumo-card");
        addThemeVariants(CardVariant.LUMO_OUTLINED, CardVariant.LUMO_ELEVATED, CardVariant.LUMO_COVER_MEDIA);
    }

    /**
     * <p>Creates a new {@code KomunumoCard} with the given title and optional image.</p>
     *
     * @param title the title text to display in the card (used also as {@code alt} text if an image is present); must not be {@code null}
     * @param image an optional image to display as media content; may be {@code null}
     */
    protected KomunumoCard(final @NotNull String title, final @Nullable ImageDto image) {
        this();
        setTitle(title);
        setImage(image, title);
    }

    /**
     * <p>Sets the image content of the card using the given {@link ImageDto} and {@code alt} text.</p>
     *
     * <p>If the {@code image} is {@code null}, no image is added. Otherwise, a new {@link Image}
     * component is created and added to the card's media slot.</p>
     *
     * @param image   the image to display; may be {@code null}
     * @param altText the alternative text for the image; must not be {@code null}
     */
    public void setImage(final @Nullable ImageDto image, final @NotNull String altText) {
        if (image != null) {
            setMedia(new Image(ImageUtil.resolveImageUrl(image), altText));
        }
    }

}
