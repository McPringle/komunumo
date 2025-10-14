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

import app.komunumo.data.dto.ConfigurationSetting;
import app.komunumo.data.service.ConfigurationService;
import app.komunumo.security.SystemAuthenticator;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * <p>Abstract base class for integration tests that run with a full Spring Boot context.</p>
 *
 * <p>It simplifies writing end-to-end or API-level integration tests by managing the
 * Spring Boot lifecycle and environment configuration.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTest {

    /**
     * <p>Injected service responsible for managing application configuration settings within the test environment.</p>
     *
     * <p>This service allows reading, updating, and clearing configuration values. It is used in integration tests to
     * dynamically adjust system settings such as the instance URL and to reset cached configuration data between
     * tests.</p>
     *
     * @see ConfigurationService
     */
    @Autowired
    private ConfigurationService configurationService;

    /**
     * <p>Injected component that provides functionality to execute actions with elevated administrative privileges
     * during tests.</p>
     *
     * <p>This authenticator is primarily used to modify restricted configuration settings or perform operations
     * that require admin-level access within the test environment.</p>
     *
     * @see SystemAuthenticator
     */
    @Autowired
    private SystemAuthenticator systemAuthenticator;

    /**
     * <p>Static instance of {@link GreenMailExtension} used to provide an in-memory SMTP server
     * for integration testing.</p>
     *
     * <p>This shared mail server allows tests to send and verify emails without requiring any
     * external SMTP infrastructure. It is configured with a single user account
     * (<code>komunumo</code>/<code>s3cr3t</code>) and persists across test methods to improve
     * performance.</p>
     */
    @RegisterExtension
    private static final @NotNull GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig()
                    .withUser("komunumo", "s3cr3t"))
            .withPerMethodLifecycle(false);

    /**
     * <p>The HTTP port on which the Spring Boot application under test is running.</p>
     *
     * <p>The port is assigned randomly by the Spring Boot test framework to prevent conflicts when multiple test
     * instances run in parallel.</p>
     */
    @LocalServerPort
    private int port;

    /**
     * <p>Holds the base URL of the running test instance, typically pointing to the local test server.</p>
     *
     * <p>This value is initialized before each test with the dynamically assigned port to ensure that
     * components depending on the instance URL can correctly resolve local endpoints.</p>
     */
    private String instanceUrl = "";

    /**
     * <p>The shared {@link ApplicationContext} instance used by all integration tests extending this class.</p>
     */
    private static ApplicationContext applicationContext;

    /**
     * <p>Injects the current {@link ApplicationContext} into this base test class.</p>
     *
     * <p>This method is automatically invoked by Spring’s dependency injection mechanism. It assigns the provided
     * context to a static variable, making it accessible to subclasses and static helper methods.</p>
     *
     * @param applicationContext the active Spring {@link ApplicationContext}, never {@code null}
     */
    @Autowired
    public void setContext(final @NotNull ApplicationContext applicationContext) {
        IntegrationTest.applicationContext = applicationContext;
    }

    /**
     * <p>Returns the {@link ApplicationContext} of the running Spring Boot test environment.</p>
     *
     * <p>This method is useful when subclasses need to retrieve beans or configuration properties from the
     * application context during test execution.</p>
     *
     * @return the active {@link ApplicationContext} instance
     */
    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * <p>Retrieves a Spring-managed bean of the specified type from the current {@link ApplicationContext}.</p>
     *
     * <p>This convenience method delegates to {@link ConfigurableApplicationContext#getBean(Class)} and provides
     * an easy way to access Spring beans in static test utilities or setup code.</p>
     *
     * @param <T> the type of bean to retrieve
     * @param clazz the class object representing the bean type, never {@code null}
     * @return the bean instance of the specified type
     * @throws BeansException if the bean could not be found or created
     * @see ConfigurableApplicationContext#getBean(Class)
     */
    protected static <T> T getBean(final @NotNull Class<T> clazz) throws BeansException {
        return applicationContext.getBean(clazz);
    }

    /**
     * <p>Returns the HTTP port on which the Spring Boot application is currently running.</p>
     *
     * <p>This method is useful for constructing URLs in integration tests that perform real HTTP requests against
     * the running application (for example, API endpoint or web interface tests).</p>
     *
     * @return the configured server port, for example {@code 8081}
     */
    protected int getPort() {
        return port;
    }

    /**
     * <p>Returns the base URL of the running test instance.</p>
     *
     * <p>The returned value typically points to the local test server and includes the dynamically assigned port. It
     * can be used by integration tests to construct absolute URLs for HTTP requests against the running instance.</p>
     *
     * @return the base URL of the current test instance
     */
    protected String getInstanceUrl() {
        return instanceUrl;
    }

    /**
     * <p>Returns the shared {@link GreenMailExtension} instance used for email testing.</p>
     *
     * <p>This method gives subclasses access to the in-memory SMTP server so that they can verify
     * sent messages, inspect inboxes, or reset the mail state between tests.</p>
     *
     * @return the global {@link GreenMailExtension} instance used by all integration tests
     */
    protected GreenMailExtension getGreenMail() {
        return greenMail;
    }

    /**
     * <p>Removes all emails from every mailbox in the shared {@link GreenMailExtension} instance.</p>
     *
     * <p>This method is automatically executed before each test to ensure a clean mail environment.
     * It prevents leftover messages from previous test runs from affecting subsequent tests.</p>
     *
     * @throws FolderException if a mailbox cannot be purged successfully
     */
    @BeforeEach
    public void purgeEmailFromAllMailboxes() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }

    /**
     * <p>Initializes the instance URL before each test based on the dynamically assigned server port.</p>
     *
     * <p>This method constructs the base URL using the current test server port and updates the corresponding
     * configuration setting within an administrative security context. It ensures that all components referencing
     * the instance URL operate with the correct local test address.</p>
     *
     * @see ConfigurationSetting#INSTANCE_URL
     * @see #getPort()
     */
    @BeforeEach
    void setInstanceUrl() {
        instanceUrl = "http://localhost:%d/".formatted(getPort());
        systemAuthenticator.runAsAdmin(() -> configurationService.setConfiguration(ConfigurationSetting.INSTANCE_URL, instanceUrl));
    }

}
