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
package org.komunumo.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.router.RouterLink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class TestUtil {

    /**
     * <p>Asserts that the given list contains exactly one instance of each specified class,
     * and that the total number of elements in the list is equal to the number of expected classes.</p>
     *
     * <p>This is useful for verifying that a component list (e.g., children in a layout)
     * contains a specific and complete set of types — each exactly once, with no extras.</p>
     *
     * <p><strong>Example usage:</strong></p>
     * <pre>{@code
     * assertContainsExactlyOneInstanceOf(components,
     *     PageHeader.class,
     *     NavigationBar.class,
     *     Main.class);
     * }</pre>
     *
     * <p><strong>Assertions:</strong></p>
     * <ul>
     *   <li>The list must contain exactly one instance of each provided class.</li>
     *   <li>The size of the list must match the number of expected classes.</li>
     * </ul>
     *
     * @param objects the list of objects to check (typically a list of UI components)
     * @param expectedClasses the expected classes that must each occur exactly once
     * @throws AssertionError if any class occurs more or fewer than once, or if unexpected elements are present
     */
    public static void assertContainsExactlyOneInstanceOf(final @NotNull  List<?> objects,
                                                          final @NotNull  Class<?>... expectedClasses) {
        assertThat(objects).hasSize(expectedClasses.length);

        for (final Class<?> expectedClass : expectedClasses) {
            final var count = objects.stream()
                    .filter(expectedClass::isInstance)
                    .count();
            assertThat(count)
                    .withFailMessage("Expected exactly one instance of '%s', but found %d.",
                            expectedClass.getSimpleName(), count)
                    .isEqualTo(1);
        }
    }

    /**
     * <p>Asserts that the given list of {@link RouterLink} components contains exactly one instance
     * for each of the specified {@link Anchor} links, matching both the text and the href.</p>
     *
     * <p>This method verifies that:</p>
     * <ul>
     *   <li>The total number of {@code routerLinks} matches the number of {@code expectedLinks}.</li>
     *   <li>Each {@code Anchor} provided in {@code expectedLinks} has exactly one corresponding {@code RouterLink}
     *       in the list, where both the link text and href are equal.</li>
     * </ul>
     *
     * <p><strong>Usage example:</strong></p>
     * <pre>{@code
     * List<RouterLink> links = ...;
     * assertContainsExactlyOneRouterLinkOf(links,
     *     new Anchor("/", "Home"),
     *     new Anchor("/about", "About"));
     * }</pre>
     *
     * @param routerLinks the actual list of {@code RouterLink} components to verify
     * @param expectedLinks the expected links, defined as {@code Anchor} instances with text and href
     * @throws AssertionError if the number of links differs, or if any expected link is missing or duplicated
     */
    public static void assertContainsExactlyOneRouterLinkOf(final @NotNull  List<RouterLink> routerLinks,
                                                            final @NotNull  Anchor... expectedLinks) {
        assertThat(routerLinks).hasSize(expectedLinks.length);

        for (final var expectedLink : expectedLinks) {
            final var text = expectedLink.getText();
            final var href = expectedLink.getHref();

            final var count = routerLinks.stream()
                    .filter(routerLink -> routerLink.getText().equals(text)
                            && routerLink.getHref().equals(href))
                    .count();
            assertThat(count)
                    .withFailMessage(
                            "Expected exactly one link with text '%s' and href '%s', but found %d.",
                            text, href, count)
                    .isEqualTo(1);
        }
    }

    /**
     * <p>Recursively searches the component tree starting from the given {@code root}
     * and returns the first component that is an instance of the specified {@code type}.</p>
     *
     * <p>This method performs a depth-first search and returns the first match found,
     * including the {@code root} component itself if it matches.</p>
     *
     * @param root the root component from which to start the search (must not be {@code null})
     * @param type the type of component to find (must not be {@code null})
     * @return the first matching component of the specified type, or {@code null} if none found
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends Component> T findComponent(final @NotNull  Component root,
                                                        final @NotNull  Class<T> type) {
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

    /**
     * <p>Recursively searches the component tree starting from the given {@code root}
     * and returns all components that are instances of the specified {@code type}.</p>
     *
     * <p>This method performs a depth-first search and returns all matches found,
     * including the {@code root} component itself if it matches.</p>
     *
     * @param root the root component from which to start the search (must not be {@code null})
     * @param type the type of components to find (must not be {@code null})
     * @return a list of all matching components of the specified type, possibly empty but never {@code null}
     */
    @NotNull
    public static <T extends Component> List<T> findComponents(final @NotNull  Component root,
                                                               final @NotNull  Class<T> type) {
        final List<T> result = new ArrayList<>();
        findComponentsRecursively(root, type, result);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Component> void findComponentsRecursively(final @NotNull  Component component,
                                                                        final @NotNull  Class<T> type,
                                                                        final @NotNull  List<T> result) {
        if (type.isInstance(component)) {
            result.add((T) component);
        }
        for (final Component child : component.getChildren().toList()) {
            findComponentsRecursively(child, type, result);
        }
    }

    private TestUtil() {
        throw new IllegalStateException("Utility class");
    }

}
