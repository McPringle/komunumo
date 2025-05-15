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
package org.komunumo.data.service;

import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;
import org.komunumo.plugin.DefaultPluginContext;
import org.komunumo.plugin.KomunumoPlugin;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class PluginService {

    private final @NotNull ServiceProvider serviceProvider;
    private final @NotNull List<KomunumoPlugin> plugins;

    public PluginService(final @NotNull ServiceProvider serviceProvider,
                         final @NotNull List<KomunumoPlugin> plugins) {
        super();
        this.serviceProvider = serviceProvider;
        this.plugins = plugins;
    }

    @PostConstruct
    public void initializePlugins() {
        final var context = new DefaultPluginContext(serviceProvider);
        plugins.forEach(plugin -> plugin.onApplicationStarted(context));
    }

}
