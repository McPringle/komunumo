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
import org.komunumo.data.service.ServiceProvider;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DefaultPluginContextTest {

    @Test
    void getLogger() {
        final PluginContext context = new DefaultPluginContext(mock(ServiceProvider.class));
        final Logger logger = context.getLogger(DefaultPluginContextTest.class);
        assertThat(logger).isNotNull();
        assertThat(logger.getName()).isEqualTo("org.komunumo.plugin.DefaultPluginContextTest");
    }

    @Test
    void getServiceProvider() {
        final ServiceProvider mockService = mock(ServiceProvider.class);
        final PluginContext context = new DefaultPluginContext(mockService);
        assertThat(context.getServiceProvider()).isSameAs(mockService);
    }

}
