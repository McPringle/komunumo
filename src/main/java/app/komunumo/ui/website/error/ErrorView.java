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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.jetbrains.annotations.NotNull;

@AnonymousAllowed
public abstract class ErrorView extends Div {

    /**
     * <p>Adds an error message to the view based on the given translation key and exception.</p>
     *
     * <p>If the exception contains a non-blank message, it is displayed directly.
     * Otherwise, a localized fallback message is retrieved using the translation key.</p>
     *
     * @param ui             the current UI instance used for translation
     * @param translationKey the key suffix used to look up the default error message
     * @param errorParameter the parameter containing the exception that triggered the error
     */
    protected void addErrorMessage(final @NotNull UI ui,
                                   final @NotNull String translationKey,
                                   final @NotNull ErrorParameter<? extends Exception> errorParameter) {
        final var defaultMessage = ui.getTranslation("error.page.%s".formatted(translationKey));
        final var customMessage = errorParameter.getException().getMessage();
        final var errorMessage = customMessage == null || customMessage.isBlank()
                ? defaultMessage
                : customMessage;
        add(new H2(errorMessage));
    }

}
