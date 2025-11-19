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
package app.komunumo.business.core.error.boundary;

import app.komunumo.business.core.config.entity.ConfigurationSetting;
import app.komunumo.business.core.config.control.ConfigurationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class NotFoundViewTest {

    @Test
    void checkErrorMessage() {

        // Arrange
        final var ui = TestUIProvider.mockUiWithTranslation("ui.views.error.ErrorView.notFound", "Test Error Message");
        final var beforeEnterEvent = mock(BeforeEnterEvent.class);
        when(beforeEnterEvent.getUI()).thenReturn(ui);
        final var configurationService = mock(ConfigurationService.class);
        when(configurationService.getConfiguration(any(ConfigurationSetting.class), any(Locale.class)))
                .thenReturn("Komunumo Test");

        try (var mockedStatic = mockStatic(UI.class)) {
            mockedStatic.when(UI::getCurrent).thenReturn(ui);

            // Act
            final var view = new NotFoundView(configurationService);
            final var errorParameter = new ErrorParameter<>(NotFoundException.class, new NotFoundException());
            final var status = view.setErrorParameter(beforeEnterEvent, errorParameter);

            // Assert
            final var h2 = (H2) view.getChildren().findFirst().orElseThrow();
            assertThat(h2.getText()).isEqualTo("Test Error Message");
            assertThat(view.getViewTitle()).isEqualTo("Test Error Message");
            assertThat(status).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private static class TestUIProvider {
        public static UI mockUiWithTranslation(final @NotNull String key, final @NotNull String value) {
            var ui = mock(UI.class);
            when(ui.getTranslation(key)).thenReturn(value);
            return ui;
        }
    }
}
