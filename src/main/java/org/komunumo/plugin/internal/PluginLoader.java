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

import org.jetbrains.annotations.NotNull;
import org.komunumo.plugin.KomunumoPlugin;
import org.komunumo.plugin.PluginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public final class PluginLoader {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(PluginLoader.class);
    private static final @NotNull Path PLUGIN_DIR =
            Path.of(System.getProperty("user.home"), ".komunumo", "plugins");

    public void loadPlugins(final @NotNull PluginContext context) {
        if (!Files.isDirectory(PLUGIN_DIR)) {
            LOGGER.info("No plugin directory found: {}", PLUGIN_DIR.toAbsolutePath());
            return;
        }

        try (Stream<Path> files = Files.list(PLUGIN_DIR)) {
            final var jarPaths = files
                    .filter(p -> p.toString().endsWith(".jar"))
                    .toList();

            if (jarPaths.isEmpty()) {
                LOGGER.info("No plugins found in: {}", PLUGIN_DIR.toAbsolutePath());
                return;
            }

            final URL[] urls = new URL[jarPaths.size()];
            for (int i = 0; i < jarPaths.size(); i++) {
                urls[i] = jarPaths.get(i).toUri().toURL();
            }

            try (URLClassLoader classLoader = new URLClassLoader(urls, KomunumoPlugin.class.getClassLoader())) {
                final var serviceLoader = ServiceLoader.load(KomunumoPlugin.class, classLoader);
                for (final KomunumoPlugin plugin : serviceLoader) {
                    LOGGER.info("Executing plugin: {}", plugin.getClass().getName());
                    plugin.onApplicationStarted(context);
                }
            }

        } catch (final IOException e) {
            LOGGER.error("Failed to load plugins: {}", e.getMessage(), e);
        }
    }
}
