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
package app.komunumo.data.dto;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ConfigurationSetting {
    INSTANCE_URL("instance.url", "http://localhost:8080"),
    INSTANCE_NAME("instance.name", "Your Instance Name"),
    INSTANCE_SLOGAN("instance.slogan", "Your Instance Slogan"),
    INSTANCE_CUSTOM_STYLES("instance.custom.styles", ""),
    INSTANCE_HIDE_COMMUNITIES("instance.hideCommunities", "false"),;

    private final String setting;
    private final String defaultValue;

    ConfigurationSetting(final @NotNull String setting, final @NotNull String defaultValue) {
        this.setting = setting;
        this.defaultValue = defaultValue;
    }

    public static ConfigurationSetting fromString(final @Nullable String settingString) {
        for (ConfigurationSetting setting : values()) {
            if (setting.setting.equals(settingString)) {
                return setting;
            }
        }
        throw new IllegalArgumentException("Unknown setting: " + settingString);
    }

    public String setting() {
        return setting;
    }

    public String defaultValue() {
        return defaultValue;
    }
}
