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
package org.komunumo.ui;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.github.mvysny.kaributesting.v10.spring.MockSpringServlet;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import kotlin.jvm.functions.Function0;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

/**
 * An abstract class which sets up Spring, Karibu-Testing and your app.
 * The easiest way to use this class in your tests is having your test class to extend
 * this class.
 */
@SpringBootTest
@DirtiesContext
public abstract class KaribuTestBase {

    private static Routes routes;

    @BeforeAll
    public static void discoverRoutes() {
        routes = new Routes().autoDiscoverViews("org.komunumo");
    }

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * @see org.junit.jupiter.api.BeforeEach
     */
    @BeforeEach
    public void setup() {
        final Function0<UI> uiFactory = UI::new;
        final var servlet = new MockSpringServlet(routes, applicationContext, uiFactory);
        MockVaadin.setup(uiFactory, servlet);
    }

    /**
     * @see org.junit.jupiter.api.AfterEach
     */
    @AfterEach
    public void tearDown() {
        MockVaadin.tearDown();
    }

    /**
     * <p>Recursively searches the component tree starting from the given {@code root}
     * and returns the first component that is an instance of the specified {@code type}.</p>
     *
     * <p></p>This method performs a depth-first search and returns the first match found,
     * including the {@code root} component itself if it matches.</p>
     *
     * @param root the root component from which to start the search (must not be {@code null})
     * @param type the type of component to find (must not be {@code null})
     * @return the first matching component of the specified type, or {@code null} if none found
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends Component> T findComponent(@NotNull final Component root,
                                                        @NotNull final Class<T> type) {
        if (type.isInstance(root)) {
            return (T) root;
        }
        for (final Component child : root.getChildren().toList()) {
            T result = findComponent(child, type);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
