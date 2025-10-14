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

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * <p>Abstract base class for integration tests that run with a full Spring Boot context.</p>
 *
 * <p>It simplifies writing end-to-end or API-level integration tests by managing the
 * Spring Boot lifecycle and environment configuration.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles("test")
public abstract class IntegrationTest {

    /**
     * <p>The HTTP port on which the Spring Boot application under test is running.</p>
     *
     * <p>The port is assigned randomly by the Spring Boot test framework to prevent conflicts when multiple test
     * instances run in parallel.</p>
     */
    @LocalServerPort
    private int port;

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

}
