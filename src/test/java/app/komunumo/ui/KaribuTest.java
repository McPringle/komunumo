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
package app.komunumo.ui;

import app.komunumo.business.core.config.entity.AppConfig;
import app.komunumo.business.user.entity.UserDto;
import app.komunumo.business.user.entity.UserRole;
import app.komunumo.business.user.entity.UserType;
import app.komunumo.business.user.control.LoginService;
import app.komunumo.business.user.control.UserService;
import app.komunumo.security.UserPrincipal;
import com.github.mvysny.fakeservlet.FakeRequest;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.github.mvysny.kaributesting.v10.spring.MockSpringServlet;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinServletRequest;
import kotlin.jvm.functions.Function0;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * An abstract class which sets up Spring, Karibu-Testing and our app.
 * The easiest way to use this class in our tests is having our test class to extend
 * this class.
 */
public abstract class KaribuTest extends IntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(KaribuTest.class);

    private static Routes routes;
    private static Path baseDataDir;

    @Autowired
    private @NotNull ApplicationContext applicationContext;

    @Autowired
    private @NotNull LoginService loginService;

    @Autowired
    private @NotNull UserService userService;

    @BeforeAll
    public static void discoverRoutes() {
        routes = new Routes().autoDiscoverViews("app.komunumo");
    }

    @BeforeAll
    public static void initDataDirectory(final @Autowired AppConfig appConfig) {
        baseDataDir = appConfig.files().basedir();
        LOGGER.info("Data directory used by tests: '{}'", baseDataDir);
    }

    @AfterAll
    public static void cleanUpDataDirectory() throws IOException {
        if (baseDataDir != null && Files.exists(baseDataDir)) {
            if (baseDataDir.toString().contains(".komunumo/test")) {
                if (!Files.exists(baseDataDir)) return;
                try (final Stream<Path> walk = Files.walk(baseDataDir)) {
                    walk.sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (final IOException e) {
                                    LOGGER.error("Failed to delete: '{}' - {}", path, e.getMessage());
                                }
                            });
                }
                LOGGER.info("Test data directory cleaned: '{}'", baseDataDir);
            } else {
                LOGGER.warn("Refusing to delete non-test data directory: '{}'", baseDataDir);
            }
        }
    }

    @BeforeEach
    public void setupMockVaadin() {
        final Function0<UI> uiFactory = UI::new;
        final var servlet = new MockSpringServlet(routes, applicationContext, uiFactory);
        MockVaadin.setup(uiFactory, servlet);
        UI.getCurrent().setLocale(Locale.ENGLISH);
    }

    /**
     * @see org.junit.jupiter.api.AfterEach
     */
    @AfterEach
    public void tearDownMockVaadin() {
        MockVaadin.tearDown();
    }

    /**
     * <p>Returns the class of the currently active Vaadin view in the UI
     * (excludes RouterLayouts like WebsiteLayout).</p>
     *
     * <p>This helper is primarily intended for use in Karibu-Testing based unit tests
     * to assert that a navigation or redirect has led to the expected view. It inspects
     * the Vaadin {@link com.vaadin.flow.component.UI#getCurrent() current UI}'s router
     * internals and retrieves the last entry of the active router target chain, which
     * represents the active view instance.</p>
     *
     * <p>Example usage:</p>
     * <pre>{@code
     * assertThat(currentViewClass()).isEqualTo(EventsView.class);
     * }</pre>
     *
     * @param <T> the expected view type (used for generic casting in tests)
     * @return the class of the currently active view (excludes RouterLayouts)
     * @throws IllegalStateException if there is no active UI or no active view
     */
    @SuppressWarnings("unchecked")
    public static <T extends Component> Class<? extends T> currentViewClass() {
        final var ui = UI.getCurrent();
        if (ui == null) {
            throw new IllegalStateException("No current UI available");
        }

        final var chain = ui.getInternals().getActiveRouterTargetsChain();
        if (chain.isEmpty()) {
            throw new IllegalStateException("No active view found in router target chain");
        }

        for (int index = chain.size() - 1; index >= 0; index--) {
            final var element = chain.get(index);
            if (!(element instanceof RouterLayout)) {
                return (Class<? extends T>) element.getClass();
            }
        }
        throw new IllegalStateException("No concrete view found (only RouterLayouts present)");
    }

    /**
     * Returns the predefined test user for the given role.
     *
     * <p>The UUIDs of the test users are fixed in {@link TestConstants} and
     * the corresponding records are inserted into the database by Flyway
     * test migrations. This method provides a convenient way for integration
     * tests to access those users without duplicating IDs.</p>
     *
     * @param role the {@link UserRole} to select (USER or ADMIN)
     * @return the {@link UserDto} for the requested role
     * @throws IllegalStateException if the user could not be found in the database
     */
    protected @NotNull UserDto getTestUser(final @NotNull UserRole role) {
        return switch (role) {
            case USER -> userService.getUserById(TestConstants.USER_ID_LOCAL)
                    .orElseThrow(() -> new IllegalStateException("Test USER not found in database"));
            case ADMIN -> userService.getUserById(TestConstants.USER_ID_ADMIN)
                    .orElseThrow(() -> new IllegalStateException("Test ADMIN not found in database"));
        };
    }

    /**
     * <p>Creates and stores a new user with a randomly generated email address and the specified role and type.</p>
     *
     * <p>The generated email address is used both as the user's login identifier and to derive a simple display name.
     * This method is intended for integration tests that require ad-hoc user accounts without relying on predefined
     * test data. The created user is immediately persisted through the {@link UserService}.</p>
     *
     * @param role the role assigned to the newly created user
     * @param type the user type to assign
     * @return the persisted {@link UserDto} instance representing the created user
     */
    protected @NotNull UserDto createRandomUser(final @NotNull UserRole role,
                                                final @NotNull UserType type) {
        final var email = getRandomEmailAddress();
        final var name = email.split("@")[0];
        final var user = new UserDto(null, null, null, null, email, name, "", null, role, type);
        return userService.storeUser(user);
    }

    /**
     * Logs in a user for integration tests by setting the Spring Security context and
     * updating the Vaadin mock request accordingly.
     *
     * @param user the user to log in
     */
    protected void login(final @NotNull UserDto user) {
        final var roleNames = List.of(user.role().getRole(), "ROLE_USER_" + user.type().name());
        final var authorities = roleNames.stream()
                .map(roleName -> (GrantedAuthority) new SimpleGrantedAuthority(roleName))
                .toList();

        // create a Spring Security user (UserDetails)
        final var userPrincipal = new UserPrincipal(user, authorities);

        // create the authentication token
        final var authentication = new PreAuthenticatedAuthenticationToken(userPrincipal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // make ViewAccessChecker work
        final var request = (FakeRequest) VaadinServletRequest.getCurrent().getRequest();
        request.setUserPrincipalInt(authentication);
        request.setUserInRole((_, role) -> roleNames.contains("ROLE_" + role));
    }

    /**
     * Logout a previously logged-in user.
     */
    protected void logout() {
        if (VaadinServletRequest.getCurrent() != null) {
            final var request = (FakeRequest) VaadinServletRequest.getCurrent().getRequest();
            request.setUserPrincipalInt(null);
            request.setUserInRole((_, _) -> false);
        }
        loginService.logout();
    }

}
