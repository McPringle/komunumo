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
package app.komunumo.ui.views.confirmation;

import app.komunumo.data.service.ConfirmationResult;
import app.komunumo.data.service.ConfirmationService;
import app.komunumo.ui.IntegrationTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.router.QueryParameters;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static app.komunumo.util.TestUtil.findComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ConfirmationViewIT extends IntegrationTest {

    @MockitoBean
    private ConfirmationService confirmationService;

    @Test
    void missingConfirmationId() {
        UI.getCurrent().navigate(ConfirmationView.class);

        final var main = _get(Main.class);
        final var h2 = findComponent(main, H2.class);
        assertThat(h2).isNotNull();
        assertThat(h2.getText()).isEqualTo("Page not found");
    }

    @Test
    void invalidConfirmationId() {
        when(confirmationService.confirm(anyString(), any()))
                .thenReturn(new ConfirmationResult(ConfirmationResult.Type.ERROR, "Invalid confirmation ID"));

        UI.getCurrent().navigate(
                ConfirmationView.class,
                new QueryParameters(Map.of("id", List.of("12345")))
        );

        final var main = _get(Main.class);
        final var h2 = findComponent(main, H2.class);
        assertThat(h2).isNotNull();
        assertThat(h2.getText()).isEqualTo("Confirmation of your email address");
        final var markdown = findComponent(main, Markdown.class);
        assertThat(markdown).isNotNull();
        assertThat(markdown.getContent()).isEqualTo("Invalid confirmation ID");
    }

    @Test
    void correctConfirmationId() {
        when(confirmationService.confirm(anyString(), any()))
                .thenReturn(new ConfirmationResult(ConfirmationResult.Type.SUCCESS, "**successful** confirmation"));

        UI.getCurrent().navigate(
                ConfirmationView.class,
                new QueryParameters(Map.of("id", List.of("12345")))
        );

        final var main = _get(Main.class);
        final var h2 = findComponent(main, H2.class);
        assertThat(h2).isNotNull();
        assertThat(h2.getText()).isEqualTo("Confirmation of your email address");
        final var markdown = findComponent(main, Markdown.class);
        assertThat(markdown).isNotNull();
        assertThat(markdown.getContent()).isEqualTo("**successful** confirmation");
    }

}
