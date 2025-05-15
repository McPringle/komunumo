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

import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("ClassCanBeRecord")
public final class DefaultPluginContext implements PluginContext {

    private final ServiceProvider serviceProvider;

    public DefaultPluginContext(final @NotNull ServiceProvider serviceProvider) {
        super();
        this.serviceProvider = serviceProvider;
    }

    @Override
    public @NotNull ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    @Override
    public @NotNull Logger getLogger(final @NotNull Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

}
