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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles("test")
public abstract class IntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ApplicationContext applicationContext;

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
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
