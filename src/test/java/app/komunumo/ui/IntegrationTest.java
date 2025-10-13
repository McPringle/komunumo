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
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTest {

    @LocalServerPort
    private int port;

    private static ApplicationContext context;

    @Autowired
    public void setContext(final @NotNull ApplicationContext context) {
        IntegrationTest.context = context;
    }

    /**
     * @see ConfigurableApplicationContext#getBean(Class)
     */
    protected static <T> T getBean(final @NotNull Class<T> clazz) throws BeansException {
        return context.getBean(clazz);
    }

    /**
     * <p>Returns the HTTP port on which the Spring Boot application is currently running.</p>
     *
     * </p>This is useful for constructing URLs in integration tests that perform real HTTP requests
     * against the running application (e.g. API endpoint tests).</p>
     *
     * @return the configured server port, e.g. {@code 8081}
     */
    protected int getPort() {
        return port;
    }

}
