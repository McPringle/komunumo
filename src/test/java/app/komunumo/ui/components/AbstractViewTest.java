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

import app.komunumo.data.dto.ConfigurationSetting;
import app.komunumo.data.service.ConfigurationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AbstractViewTest {

    private ConfigurationService configurationService;

    @BeforeEach
    void setUp() {
        configurationService = mock(ConfigurationService.class);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    void shouldUseInstanceNameForPageTitle() {
        when(configurationService.getConfiguration(ConfigurationSetting.INSTANCE_NAME))
                .thenReturn("Komunumo Test");

        final var view = new TestView(configurationService);
        assertThat(view.getPageTitle()).isEqualTo("Test View – Komunumo Test");
    }

    @Test
    void updatePageTitleSuccess() {
        final var page = mock(Page.class);
        final var ui = new TestUI(page);
        UI.setCurrent(ui);

        when(configurationService.getConfiguration(ConfigurationSetting.INSTANCE_NAME))
                .thenReturn("Komunumo Test");

        final var view = new TestView(configurationService);
        ui.add(view);
        view.updatePageTitle();
        verify(page).setTitle("Test View – Komunumo Test");
    }

    @Test
    void updatePageTitle_doNothing() {
        UI.setCurrent(null); // no UI available
        final var view = new TestView(configurationService);
        assertThatCode(view::updatePageTitle).doesNotThrowAnyException();
        verifyNoInteractions(configurationService);
    }

    private static class TestView extends AbstractView {
        protected TestView(ConfigurationService configurationService) {
            super(configurationService);
        }

        @Override
        protected @NotNull String getViewTitle() {
            return "Test View";
        }
    }

    private static final class TestUI extends UI {

        private final Page page;

        TestUI(final @NotNull Page page) {
            this.page = page;
        }

        @Override
        public @NotNull Page getPage() {
            return page;
        }
    }

}
