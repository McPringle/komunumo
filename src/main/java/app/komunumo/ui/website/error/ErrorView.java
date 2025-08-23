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
package app.komunumo.ui.website.error;

import app.komunumo.data.service.ConfigurationService;
import app.komunumo.ui.components.AbstractView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;

@AnonymousAllowed
abstract class ErrorView extends AbstractView {

    private final @NotNull String viewTitle;

    /**
     * <p>Creates a new error view based on the provided error type with access to the configuration service.</p>
     *
     * @param configurationService the configuration service used to resolve the instance name;
     *                             must not be {@code null}
     */
    protected ErrorView(final @NotNull ErrorType errorType,
                        final @NotNull ConfigurationService configurationService) {
        super(configurationService);
        final var ui = UI.getCurrent();
        this.viewTitle = ui.getTranslation("error.page." + errorType.getTranslationKey());
        add(new H2(viewTitle));
    }

    @Override
    protected @NotNull String getViewTitle() {
        return viewTitle;
    }

}
