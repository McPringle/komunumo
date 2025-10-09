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
package app.komunumo.ui.components;

import app.komunumo.data.service.ConfigurationService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import org.jetbrains.annotations.NotNull;

import static app.komunumo.data.dto.ConfigurationSetting.INSTANCE_NAME;

public abstract class AbstractView extends VerticalLayout implements HasDynamicTitle {

    private final transient @NotNull ConfigurationService configurationService;

    /**
     * <p>Creates a new view instance with access to the configuration service for
     * retrieving localized configuration values such as the instance name.</p>
     *
     * @param configurationService the configuration service used to resolve the instance name;
     *                             must not be {@code null}
     */
    protected AbstractView(final @NotNull ConfigurationService configurationService) {
        super();
        this.configurationService = configurationService;
    }

    /**
     * <p>Returns the view-specific title part that appears in the page title before the instance name.</p>
     *
     * <p>Example: For an events page, this might return {@code "Events"}, which will result in a
     * full page title like {@code "Events – Komunumo"}.</p>
     *
     * @return the view-specific title; must not be {@code null}
     */
    protected abstract @NotNull String getViewTitle();

    /**
     * <p>Returns the full page title to be shown in the browser tab, composed of the view title
     * (provided by {@link #getViewTitle()}) and the configured instance name for the current locale.</p>
     *
     * <p>The format is {@code "View title – Instance name"}, e.g. {@code "Events – Komunumo"}.
     * The instance name is resolved using the {@link ConfigurationService}.</p>
     *
     * @return the complete page title; never {@code null}
     */
    @Override
    public @NotNull String getPageTitle() {
        final var instanceName = configurationService.getConfiguration(INSTANCE_NAME);
        return getViewTitle() + " – " + instanceName;
    }

    /**
     * <p>Updates the browser page title based on the current value returned
     * by {@link #getPageTitle()}.</p>
     *
     * <p>Normally, Vaadin sets the page title automatically during navigation
     * by calling {@link HasDynamicTitle#getPageTitle()}. However, if the view
     * content and title are updated dynamically without triggering a new
     * navigation, the browser title will not change automatically.</p>
     *
     * <p>This helper method allows views to explicitly refresh the title shown
     * in the browser tab. It retrieves the current UI and calls
     * {@code getPageTitle()}. If no UI is available, the call is ignored.</p>
     */
    protected void updatePageTitle() {
        getUI().ifPresent(ui -> ui.getPage().setTitle(getPageTitle()));
    }

}
