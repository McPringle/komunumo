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
package app.komunumo.domain.core.exporter.control;

import app.komunumo.KomunumoException;
import app.komunumo.domain.community.control.CommunityService;
import app.komunumo.domain.core.config.control.ConfigurationService;
import app.komunumo.domain.core.config.entity.ConfigurationSetting;
import app.komunumo.domain.core.i18n.controller.TranslationProvider;
import app.komunumo.domain.core.image.control.ImageService;
import app.komunumo.domain.core.mail.control.MailService;
import app.komunumo.domain.event.control.EventService;
import app.komunumo.domain.member.control.MemberService;
import app.komunumo.domain.page.control.GlobalPageService;
import app.komunumo.domain.participant.control.ParticipantService;
import app.komunumo.domain.user.control.UserService;
import app.komunumo.util.ImageUtil;
import app.komunumo.util.LocaleUtil;
import org.jetbrains.annotations.NotNull;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

/**
 * <p>Exports all Komunumo instance data as a JSON file.</p>
 *
 * <p>This exporter creates a JSON structure compatible with the {@code JSONImporter},
 * allowing backup and transfer of instance configurations and content.</p>
 */
public final class JSONExporter {

    private final @NotNull ObjectMapper objectMapper;

    public JSONExporter() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * <p>Exports all instance data as a JSON string.</p>
     *
     * @param configurationService service for configuration data
     * @param imageService service for image data
     * @param userService service for user data
     * @param communityService service for community data
     * @param memberService service for member data
     * @param eventService service for event data
     * @param participantService service for participant data
     * @param globalPageService service for global page data
     * @param mailService service for mail template data
     * @return a pretty-printed JSON string containing all exported data
     */
    @SuppressWarnings({"java:S107", "checkstyle:ParameterNumber"}) // Number of parameters is justified for complete export
    public String exportAll(
            final @NotNull ConfigurationService configurationService,
            final @NotNull ImageService imageService,
            final @NotNull UserService userService,
            final @NotNull CommunityService communityService,
            final @NotNull MemberService memberService,
            final @NotNull EventService eventService,
            final @NotNull ParticipantService participantService,
            final @NotNull GlobalPageService globalPageService,
            final @NotNull MailService mailService,
            final @NotNull TranslationProvider translationProvider
            ) {
        try {
            final ObjectNode root = objectMapper.createObjectNode();

            exportSettings(root, configurationService, translationProvider);
            exportImages(root, imageService);
            exportUsers(root, userService);
            exportCommunities(root, communityService);
            exportEvents(root, eventService);
            exportMembers(root, memberService);
            exportParticipants(root, participantService);
            exportGlobalPages(root, globalPageService);
            exportMailTemplates(root, mailService);

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (final Exception e) {
            throw new KomunumoException(e.getMessage(), e);
        }
    }

    private void exportSettings(final @NotNull ObjectNode root,
                                final @NotNull ConfigurationService configurationService,
                                final @NotNull TranslationProvider translationProvider) {
        final ArrayNode settingsArray = objectMapper.createArrayNode();

        for (final var configurationSetting : ConfigurationSetting.values()) {
            if (configurationSetting.isLanguageDependent()) {
                for (final var locale : translationProvider.getProvidedLocales()) {
                    final var defaultValue = configurationSetting.defaultValue();
                    final var actualValue = configurationService.getConfigurationWithoutFallback(configurationSetting, locale);
                    final var isDefaultValue = defaultValue.equals(actualValue);

                    if (isDefaultValue) {
                        continue; // skip default values to reduce export size
                    }

                    final ObjectNode node = objectMapper.createObjectNode();
                    node.put("setting", configurationSetting.setting());
                    node.put("language", LocaleUtil.getLanguageCode(locale));
                    node.put("value", actualValue);
                    settingsArray.add(node);
                }
            } else {
                final var defaultValue = configurationSetting.defaultValue();
                final var actualValue = configurationService.getConfigurationWithoutFallback(configurationSetting, null);
                final var isDefaultValue = defaultValue.equals(actualValue);

                if (isDefaultValue) {
                    continue; // skip default values to reduce export size
                }

                final ObjectNode node = objectMapper.createObjectNode();
                node.put("setting", configurationSetting.setting());
                node.put("value", actualValue);
                settingsArray.add(node);
            }
        }

        root.set("settings", settingsArray);
    }

    private void exportImages(final @NotNull ObjectNode root,
                              final @NotNull ImageService imageService) {
        final ArrayNode imagesArray = objectMapper.createArrayNode();
        imageService.getAllImages().forEach(image -> {
            final ObjectNode node = objectMapper.createObjectNode();
            node.put("imageId", image.id().toString());
            node.put("contentType", image.contentType().getContentType());

            // Include base64-encoded image data for complete export
            final var imagePath = ImageUtil.resolveImagePath(image);
            if (imagePath != null) {
                if (Files.exists(imagePath)) {
                    try {
                        final byte[] imageData = Files.readAllBytes(imagePath);
                        node.put("data", Base64.getEncoder().encodeToString(imageData));
                    } catch (final IOException e) {
                        // Skip image data if file cannot be read
                        node.putNull("data");
                    }
                } else {
                    node.putNull("data");
                }
            } else {
                node.putNull("data");
            }
            imagesArray.add(node);
        });
        root.set("images", imagesArray);
    }

    private void exportUsers(final @NotNull ObjectNode root,
                             final @NotNull UserService userService) {
        final ArrayNode usersArray = objectMapper.createArrayNode();
        userService.getAllUsers().forEach(user -> {
            final ObjectNode node = objectMapper.createObjectNode();
            node.put("userId", user.id().toString());
            node.put("profile", user.profile());
            node.put("email", user.email() != null ? user.email() : "");
            node.put("name", user.name() != null ? user.name() : "");
            node.put("bio", user.bio() != null ? user.bio() : "");
            node.put("imageId", user.imageId() != null ? user.imageId().toString() : "");
            node.put("role", user.role().name());
            node.put("type", user.type().name());
            usersArray.add(node);
        });
        root.set("users", usersArray);
    }

    private void exportCommunities(final @NotNull ObjectNode root,
                                   final @NotNull CommunityService communityService) {
        final ArrayNode communitiesArray = objectMapper.createArrayNode();
        communityService.getCommunities().forEach(community -> {
            final ObjectNode node = objectMapper.createObjectNode();
            node.put("communityId", community.id().toString());
            node.put("profile", community.profile());
            node.put("name", community.name());
            node.put("description", community.description());
            node.put("imageId", community.imageId() != null ? community.imageId().toString() : "");
            communitiesArray.add(node);
        });
        root.set("communities", communitiesArray);
    }

    private void exportEvents(final @NotNull ObjectNode root,
                              final @NotNull EventService eventService) {
        final ArrayNode eventsArray = objectMapper.createArrayNode();
        eventService.getEvents().forEach(event -> {
            final ObjectNode node = objectMapper.createObjectNode();
            node.put("eventId", event.id().toString());
            node.put("communityId", event.communityId() != null ? event.communityId().toString() : "");
            node.put("title", event.title());
            node.put("description", event.description());
            node.put("location", event.location());
            node.put("begin", event.begin() != null ? event.begin().toString() : "");
            node.put("end", event.end() != null ? event.end().toString() : "");
            node.put("imageId", event.imageId() != null ? event.imageId().toString() : "");
            node.put("visibility", event.visibility().name());
            node.put("status", event.status().name());
            eventsArray.add(node);
        });
        root.set("events", eventsArray);
    }

    private void exportMembers(final @NotNull ObjectNode root,
                               final @NotNull MemberService memberService) {
        final ArrayNode membersArray = objectMapper.createArrayNode();
        memberService.getMembers().forEach(member -> {
            final ObjectNode node = objectMapper.createObjectNode();
            node.put("userId", member.userId().toString());
            node.put("communityId", member.communityId().toString());
            node.put("role", member.role().name());
            node.put("since", member.since() != null ? member.since().toString() : "");
            membersArray.add(node);
        });
        root.set("members", membersArray);
    }

    private void exportParticipants(final @NotNull ObjectNode root,
                                    final @NotNull ParticipantService participantService) {
        final ArrayNode participantsArray = objectMapper.createArrayNode();
        participantService.getAllParticipants().forEach(participant -> {
            final ObjectNode node = objectMapper.createObjectNode();
            node.put("eventId", participant.eventId().toString());
            node.put("userId", participant.userId().toString());
            node.put("registered", participant.registered() != null ? participant.registered().toString() : "");
            participantsArray.add(node);
        });
        root.set("participants", participantsArray);
    }

    private void exportGlobalPages(final @NotNull ObjectNode root,
                                   final @NotNull GlobalPageService globalPageService) {
        final ArrayNode globalPagesArray = objectMapper.createArrayNode();
        globalPageService.getAllGlobalPages().forEach(page -> {
            final ObjectNode node = objectMapper.createObjectNode();
            node.put("slot", page.slot());
            node.put("language", page.language().toLanguageTag());
            node.put("title", page.title());
            node.put("markdown", page.markdown());
            globalPagesArray.add(node);
        });
        root.set("globalPages", globalPagesArray);
    }

    private void exportMailTemplates(final @NotNull ObjectNode root,
                                     final @NotNull MailService mailService) {
        final ArrayNode mailTemplatesArray = objectMapper.createArrayNode();
        mailService.getAllMailTemplates().forEach(template -> {
            final ObjectNode node = objectMapper.createObjectNode();
            node.put("mailTemplateId", template.id().name());
            node.put("language", template.language().toLanguageTag());
            node.put("subject", template.subject());
            node.put("markdown", template.markdown());
            mailTemplatesArray.add(node);
        });
        root.set("mailTemplates", mailTemplatesArray);
    }
}
