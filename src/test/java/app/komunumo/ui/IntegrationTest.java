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

import app.komunumo.configuration.AppConfig;
import app.komunumo.data.dto.UserDto;
import app.komunumo.data.dto.UserRole;
import app.komunumo.data.service.LoginService;
import com.github.mvysny.fakeservlet.FakeRequest;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.github.mvysny.kaributesting.v10.Routes;
import com.github.mvysny.kaributesting.v10.spring.MockSpringServlet;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import kotlin.jvm.functions.Function0;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.annotation.DirtiesContext;

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
@SpringBootTest
@DirtiesContext
public abstract class IntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTest.class);

    @RegisterExtension
    protected static final @NotNull GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig()
                    .withUser("komunumo", "s3cr3t"))
            .withPerMethodLifecycle(false);

    private static Routes routes;
    private static Path baseDataDir;
    @Autowired
    private LoginService loginService;

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

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    public void setup() throws FolderException {
        final Function0<UI> uiFactory = UI::new;
        final var servlet = new MockSpringServlet(routes, applicationContext, uiFactory);
        MockVaadin.setup(uiFactory, servlet);
        UI.getCurrent().setLocale(Locale.ENGLISH);
        greenMail.purgeEmailFromAllMailboxes();
    }

    /**
     * @see org.junit.jupiter.api.AfterEach
     */
    @AfterEach
    public void tearDown() {
        MockVaadin.tearDown();
    }

    /**
     * Logs in a user for integration tests by setting the Spring Security context and
     * updating the Vaadin mock request accordingly.
     *
     * @param user the user to log in
     */
    protected void login(final @NotNull UserDto user) {
        final var roles = List.of(user.role());
        final var authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();

        // create a Spring Security user (UserDetails)
        final var userDetails = new User(user.email(), null, authorities);

        // create the authentication token
        final var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // make ViewAccessChecker work
        final var request = (FakeRequest) VaadinServletRequest.getCurrent().getRequest();
        request.setUserPrincipalInt(authentication);
        request.setUserInRole((principal, role) -> roles.contains(UserRole.valueOf(role)));

        UI.getCurrent().getPage().reload();
    }

    /**
     * Logout a previously logged-in user.
     */
    protected void logout() {
        if (VaadinServletRequest.getCurrent() != null) {
            final var request = (FakeRequest) VaadinServletRequest.getCurrent().getRequest();
            request.setUserPrincipalInt(null);
            request.setUserInRole((principal, role) -> false);
        }
        loginService.logout();
        UI.getCurrent().getPage().reload();
    }

}
