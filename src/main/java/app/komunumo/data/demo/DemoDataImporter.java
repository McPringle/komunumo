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
package app.komunumo.data.demo;

import app.komunumo.data.dto.CommunityDto;
import app.komunumo.data.dto.ConfigurationSetting;
import app.komunumo.data.dto.ContentType;
import app.komunumo.data.dto.EventDto;
import app.komunumo.data.dto.EventStatus;
import app.komunumo.data.dto.EventVisibility;
import app.komunumo.data.dto.GlobalPageDto;
import app.komunumo.data.dto.ImageDto;
import app.komunumo.data.service.CommunityService;
import app.komunumo.data.service.ConfigurationService;
import app.komunumo.data.service.EventService;
import app.komunumo.data.service.GlobalPageService;
import app.komunumo.data.service.ImageService;
import app.komunumo.util.DownloadUtil;
import app.komunumo.util.ImageUtil;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings("java:S1192") // Suppressing "String literals should not be duplicated" because of different contexts
public final class DemoDataImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoDataImporter.class);

    private final @NotNull JSONObject demoData;

    public DemoDataImporter(final @NotNull String demoDataUrl) {
        this.demoData = downloadJsonObject(demoDataUrl);
    }

    private @NotNull JSONObject downloadJsonObject(final @NotNull String demoDataUrl) {
        try {
            final String json = DownloadUtil.getString(demoDataUrl);
            final JSONObject jsonObject = new JSONObject(json);
            LOGGER.info("Successfully loaded {} settings, {} communities, {} events, {} images, and {} global pages",
                    jsonObject.getJSONArray("settings").length(),
                    jsonObject.getJSONArray("communities").length(),
                    jsonObject.getJSONArray("events").length(),
                    jsonObject.getJSONArray("images").length(),
                    jsonObject.getJSONArray("globalPages").length());
            return jsonObject;
        } catch (IOException | URISyntaxException e) {
            LOGGER.warn("Failed to download demo data: {}", e.getMessage());
        }
        return new JSONObject();
    }

    public void importSettings(final @NotNull ConfigurationService configurationService) {
        if (demoData.has("settings")) {
            demoData.getJSONArray("settings").forEach(object -> {
                final var jsonObject = (JSONObject) object;
                final var setting = ConfigurationSetting.fromString(jsonObject.getString("setting"));
                final var locale = Locale.forLanguageTag(jsonObject.getString("language"));
                final var value = jsonObject.getString("value");

                configurationService.setConfiguration(setting, locale, value);
            });
            configurationService.clearCache();
        } else {
            LOGGER.warn("No settings found in demo data.");
        }
    }

    public void importImages(final @NotNull ImageService imageService) {
        if (demoData.has("images")) {
            demoData.getJSONArray("images").forEach(object -> {
                final var jsonObject = (JSONObject) object;
                final var imageId = UUID.fromString(jsonObject.getString("imageId"));
                final var contentType = ContentType.fromContentType(jsonObject.getString("contentType"));

                final var url = jsonObject.getString("url");
                final var path = DownloadUtil.downloadFile(url);

                if (path != null) {
                    final var image = new ImageDto(imageId, contentType);
                    try {
                        ImageUtil.storeImage(image, path);
                        imageService.storeImage(image);
                    } catch (final IOException e) {
                        LOGGER.error("Failed to import image: {}", e.getMessage());
                    }
                }
            });
        } else {
            LOGGER.warn("No images found in demo data.");
        }
    }

    public void importCommunities(final @NotNull CommunityService communityService) {
        if (demoData.has("communities")) {
            demoData.getJSONArray("communities").forEach(object -> {
                final var jsonObject = (JSONObject) object;
                final var communityId = UUID.fromString(jsonObject.getString("communityId"));
                final var profile = jsonObject.getString("profile");
                final var name = jsonObject.getString("name");
                final var description = jsonObject.getString("description");
                final var imageId = jsonObject.getString("imageId");
                final var imageUUID = imageId.isBlank() ? null : UUID.fromString(imageId);

                final var community = new CommunityDto(communityId, profile, null, null,
                        name, description, imageUUID);
                communityService.storeCommunity(community);
            });
        } else {
            LOGGER.warn("No communities found in demo data.");
        }
    }

    public void importEvents(final @NotNull EventService eventService) {
        if (demoData.has("events")) {
            demoData.getJSONArray("events").forEach(object -> {
                final var jsonObject = (JSONObject) object;
                final var eventId = UUID.fromString(jsonObject.getString("eventId"));
                final var communityId = UUID.fromString(jsonObject.getString("communityId"));
                final var title = jsonObject.getString("title");
                final var description = jsonObject.getString("description");
                final var location = jsonObject.getString("location");
                final var begin = ZonedDateTime.parse(jsonObject.getString("begin"));
                final var end = ZonedDateTime.parse(jsonObject.getString("end"));
                final var imageId = jsonObject.getString("imageId");
                final var imageUUID = imageId.isBlank() ? null : UUID.fromString(imageId);
                final var visibility = EventVisibility.valueOf(jsonObject.getString("visibility"));
                final var status = EventStatus.valueOf(jsonObject.getString("status"));

                final var event = new EventDto(eventId, communityId, null, null,
                        title, description, location, begin, end, imageUUID, visibility, status);
                eventService.storeEvent(event);
            });
        } else {
            LOGGER.warn("No events found in demo data.");
        }
    }

    public void importGlobalPages(final @NotNull GlobalPageService globalPageService) {
        if (demoData.has("globalPages")) {
            demoData.getJSONArray("globalPages").forEach(object -> {
                final var jsonObject = (JSONObject) object;
                final var slot = jsonObject.getString("slot");
                final var locale = Locale.forLanguageTag(jsonObject.getString("language"));
                final var title = jsonObject.getString("title");
                final var markdown = jsonObject.getString("markdown");

                final var globalPage = new GlobalPageDto(slot, locale, null, null, title, markdown);
                globalPageService.storeGlobalPage(globalPage);
            });
        } else {
            LOGGER.warn("No global pages found in demo data.");
        }
    }
}
