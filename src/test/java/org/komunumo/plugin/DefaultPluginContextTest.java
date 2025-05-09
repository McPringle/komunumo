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
package org.komunumo.plugin;

import org.junit.jupiter.api.Test;
import org.komunumo.data.service.DatabaseService;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class DefaultPluginContextTest {

    @Test
    void getLogger() {
        final PluginContext context = new DefaultPluginContext(mock(DatabaseService.class));
        final Logger logger = context.getLogger(DefaultPluginContextTest.class);
        assertNotNull(logger);
        assertEquals("org.komunumo.plugin.DefaultPluginContextTest", logger.getName());
    }

    @Test
    void getDatabaseService() {
        final DatabaseService mockService = mock(DatabaseService.class);
        final PluginContext context = new DefaultPluginContext(mockService);
        assertSame(mockService, context.getDatabaseService());
    }

}
