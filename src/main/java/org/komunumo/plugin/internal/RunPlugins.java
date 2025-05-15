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
package org.komunumo.plugin.internal;

import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;
import org.komunumo.data.service.ServiceProvider;
import org.komunumo.plugin.DefaultPluginContext;
import org.komunumo.plugin.PluginContext;
import org.springframework.stereotype.Service;

@Service
public final class RunPlugins {

    private final @NotNull ServiceProvider serviceProvider;

    public RunPlugins(final @NotNull ServiceProvider serviceProvider) {
        super();
        this.serviceProvider = serviceProvider;
    }

    @PostConstruct
    public void runExtensions() {
        final PluginContext context = new DefaultPluginContext(serviceProvider);
        new PluginLoader().loadPlugins(context);
    }

}
