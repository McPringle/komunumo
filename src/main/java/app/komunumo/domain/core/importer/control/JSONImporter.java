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
package app.komunumo.domain.core.importer.control;

import app.komunumo.KomunumoException;
import app.komunumo.domain.event.entity.EventDto;
import app.komunumo.domain.event.entity.EventStatus;
import app.komunumo.domain.event.entity.EventVisibility;
import app.komunumo.domain.community.entity.CommunityDto;
import app.komunumo.domain.core.config.entity.ConfigurationSetting;
import app.komunumo.domain.core.image.entity.ContentType;
import app.komunumo.domain.page.entity.GlobalPageDto;
import app.komunumo.domain.core.image.entity.ImageDto;
import app.komunumo.domain.member.entity.MemberDto;
import app.komunumo.domain.member.entity.MemberRole;
import app.komunumo.domain.user.entity.UserDto;
import app.komunumo.domain.user.entity.UserRole;
import app.komunumo.domain.user.entity.UserType;
import app.komunumo.domain.community.control.CommunityService;
import app.komunumo.domain.core.config.control.ConfigurationService;
import app.komunumo.domain.event.control.EventService;
import app.komunumo.domain.page.control.GlobalPageService;
import app.komunumo.domain.core.image.control.ImageService;
import app.komunumo.domain.member.control.MemberService;
import app.komunumo.domain.user.control.UserService;
import app.komunumo.util.DownloadUtil;
import app.komunumo.util.ImageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("java:S1192") // Suppressing "String literals should not be duplicated" because of different contexts
public final class JSONImporter {

    private final @NotNull ImporterLog importerLog;
    private final @NotNull JSONObject jsonData;

    public JSONImporter(final @NotNull ImporterLog importerLog,
                        final @NotNull String jsonDataUrl) {
        this.importerLog = importerLog;
        try {
            final String json = DownloadUtil.getString(jsonDataUrl);
            final JSONObject jsonObject = new JSONObject(json);
            logJSONInfo(jsonObject);
            jsonData = jsonObject;
        } catch (IOException | URISyntaxException e) {
            importerLog.error("Failed to download JSON data from URL: %s".formatted(jsonDataUrl));
            throw new KomunumoException("Failed to download JSON data from URL: %s".formatted(jsonDataUrl), e);
        }
    }

    public JSONImporter(final @NotNull ImporterLog importerLog,
                        final @NotNull File jsonDataFile) {
        this.importerLog = importerLog;
        try {
            final String json = Files.readString(jsonDataFile.toPath());
            final JSONObject jsonObject = new JSONObject(json);
            logJSONInfo(jsonObject);
            jsonData = jsonObject;
        } catch (IOException e) {
            importerLog.error("Failed to download JSON data from URL: %s".formatted(jsonDataFile.getName()));
            throw new KomunumoException("Failed to load JSON data from file: %s".formatted(jsonDataFile.getName()), e);
        }
    }

    private void logJSONInfo(final @NotNull JSONObject jsonObject) {
        importerLog.info("Identified %d settings, %d images, %d users, %d communities, %d events, %d members, and %d global pages."
                .formatted(
                        countArrayItems(jsonObject, "settings"),
                        countArrayItems(jsonObject, "images"),
                        countArrayItems(jsonObject, "users"),
                        countArrayItems(jsonObject, "communities"),
                        countArrayItems(jsonObject, "events"),
                        countArrayItems(jsonObject, "members"),
                        countArrayItems(jsonObject, "globalPages")));
    }

    private static int countArrayItems(final @NotNull JSONObject jsonData,
                                       final @NotNull String arrayName) {
        if (jsonData.has(arrayName)) {
            return jsonData.getJSONArray(arrayName).length();
        }
        return 0;
    }

    public void importSettings(final @NotNull ConfigurationService configurationService) {
        if (jsonData.has("settings")) {
            final var counter = new AtomicInteger(0);
            importerLog.info("Start importing settings...");
            jsonData.getJSONArray("settings").forEach(object -> {
                final var jsonObject = (JSONObject) object;
                final var setting = ConfigurationSetting.fromString(jsonObject.getString("setting"));
                final var language = jsonObject.optString("language", null);

                if (setting.isLanguageDependent() && language == null) {
                    importerLog.warn("Skipping setting '%s' because it is language-dependent but no language was provided."
                            .formatted(setting.setting()));
                    return;
                } else if (!setting.isLanguageDependent() && language != null) {
                    importerLog.warn("Skipping setting '%s' because it is not language-dependent but a language was provided."
                            .formatted(setting.setting()));
                    return;
                }

                final var locale = language == null ? null : Locale.forLanguageTag(language);
                final var value = jsonObject.getString("value");
                configurationService.setConfiguration(setting, locale, value);
                counter.incrementAndGet();
            });
            configurationService.clearCache();
            importerLog.info("...finished importing %d settings.".formatted(counter.get()));
        } else {
            importerLog.warn("No settings found in JSON data.");
        }
    }

    public void importUsers(final @NotNull UserService userService) {
        if (jsonData.has("users")) {
            final var counter = new AtomicInteger(0);
            importerLog.info("Start importing users...");
            jsonData.getJSONArray("users").forEach(object -> {
                final var jsonObject = (JSONObject) object;
                final var userId = UUID.fromString(jsonObject.getString("userId"));
                final var profile = jsonObject.getString("profile").trim();
                final var email = jsonObject.getString("email").trim();
                final var name = jsonObject.getString("name").trim();
                final var bio = jsonObject.getString("bio").trim();
                final var imageId = parseUUID(jsonObject.optString("imageId"));
                final var role = UserRole.valueOf(jsonObject.getString("role").trim());
                final var type = UserType.valueOf(jsonObject.getString("type").trim());

                final var user = new UserDto(userId, null, null, profile, email, name, bio, imageId,
                        role, type);
                userService.storeUser(user);
                counter.incrementAndGet();
            });
            importerLog.info("...finished importing %d users.".formatted(counter.get()));
        } else {
            importerLog.warn("No users found in JSON data.");
        }
    }

    public void importImages(final @NotNull ImageService imageService) {
        if (jsonData.has("images")) {
            final var counter = new AtomicInteger(0);
            importerLog.info("Start importing images...");
            jsonData.getJSONArray("images").forEach(object -> {
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
                        importerLog.error("Failed to import image: %s".formatted(e.getMessage()));
                    }
                }
                counter.incrementAndGet();
            });
            importerLog.info("...finished importing %d images.".formatted(counter.get()));
        } else {
            importerLog.warn("No images found in JSON data.");
        }
    }

    public void importCommunities(final @NotNull CommunityService communityService) {
        if (jsonData.has("communities")) {
            final var counter = new AtomicInteger(0);
            importerLog.info("Start importing communities...");
            jsonData.getJSONArray("communities").forEach(object -> {
                final var jsonObject = (JSONObject) object;
                final var communityId = UUID.fromString(jsonObject.getString("communityId"));
                final var profile = jsonObject.getString("profile").trim();
                final var name = jsonObject.getString("name").trim();
                final var description = jsonObject.getString("description").trim();
                final var imageId = parseUUID(jsonObject.optString("imageId"));

                final var community = new CommunityDto(communityId, profile, null, null,
                        name, description, imageId);
                communityService.storeCommunity(community);
                counter.incrementAndGet();
            });
            importerLog.info("...finished importing %d communities.".formatted(counter.get()));
        } else {
            importerLog.warn("No communities found in JSON data.");
        }
    }

    public void importEvents(final @NotNull EventService eventService) {
        if (jsonData.has("events")) {
            final var counter = new AtomicInteger(0);
            importerLog.info("Start importing events...");
            jsonData.getJSONArray("events").forEach(object -> {
                final var jsonObject = (JSONObject) object;
                final var eventId = UUID.fromString(jsonObject.getString("eventId"));
                final var communityId = UUID.fromString(jsonObject.getString("communityId"));
                final var title = jsonObject.getString("title").trim();
                final var description = jsonObject.getString("description").trim();
                final var location = jsonObject.getString("location").trim();
                final var begin = parseDateTime(jsonObject.optString("begin", ""));
                final var end = parseDateTime(jsonObject.optString("end", ""));
                final var imageId = parseUUID(jsonObject.optString("imageId", ""));
                final var visibility = EventVisibility.valueOf(jsonObject.getString("visibility"));
                final var status = EventStatus.valueOf(jsonObject.getString("status"));

                final var event = new EventDto(eventId, communityId, null, null,
                        title, description, location, begin, end, imageId, visibility, status);
                eventService.storeEvent(event);
                counter.incrementAndGet();
            });
            importerLog.info("...finished importing %d events.".formatted(counter.get()));
        } else {
            importerLog.warn("No events found in JSON data.");
        }
    }

    public void importMembers(final @NotNull MemberService memberService) {
        if (jsonData.has("members")) {
            final var counter = new AtomicInteger(0);
            importerLog.info("Start importing members...");
            jsonData.getJSONArray("members").forEach(object -> {
                final var jsonObject = (JSONObject) object;
                final var userId = UUID.fromString(jsonObject.getString("userId"));
                final var communityId = UUID.fromString(jsonObject.getString("communityId"));
                final var role = MemberRole.valueOf(jsonObject.getString("role"));
                final var since = parseDateTime(jsonObject.optString("since", ""));

                final var member = new MemberDto(userId, communityId, role, since);
                memberService.storeMember(member);
                counter.incrementAndGet();
            });
            importerLog.info("...finished importing %d events.".formatted(counter.get()));
        } else {
            importerLog.warn("No members found in JSON data.");
        }
    }

    public void importGlobalPages(final @NotNull GlobalPageService globalPageService) {
        if (jsonData.has("globalPages")) {
            final var counter = new AtomicInteger(0);
            importerLog.info("Start importing global pages...");
            jsonData.getJSONArray("globalPages").forEach(object -> {
                final var jsonObject = (JSONObject) object;
                final var slot = jsonObject.getString("slot").trim();
                final var locale = Locale.forLanguageTag(jsonObject.getString("language"));
                final var title = jsonObject.getString("title").trim();
                final var markdown = jsonObject.getString("markdown").trim();

                final var globalPage = new GlobalPageDto(slot, locale, null, null, title, markdown);
                globalPageService.storeGlobalPage(globalPage);
                counter.incrementAndGet();
            });
            importerLog.info("...finished importing %d global pages.".formatted(counter.get()));
        } else {
            importerLog.warn("No global pages found in JSON data.");
        }
    }

    private @Nullable UUID parseUUID(final @NotNull String uuidString) {
        return uuidString.isBlank() ? null : UUID.fromString(uuidString);
    }

    private @Nullable ZonedDateTime parseDateTime(final @NotNull String dateTime) {
        return dateTime.isBlank() ? null : ZonedDateTime.parse(dateTime);
    }

}
