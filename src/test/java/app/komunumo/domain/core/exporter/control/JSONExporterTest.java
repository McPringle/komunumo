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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.komunumo.domain.community.control.CommunityService;
import app.komunumo.domain.community.entity.CommunityDto;
import app.komunumo.domain.core.config.control.ConfigurationService;
import app.komunumo.domain.core.config.entity.ConfigurationValue;
import app.komunumo.domain.core.image.control.ImageService;
import app.komunumo.domain.core.image.entity.ContentType;
import app.komunumo.domain.core.image.entity.ImageDto;
import app.komunumo.domain.core.mail.control.MailService;
import app.komunumo.domain.core.mail.entity.MailTemplate;
import app.komunumo.domain.core.mail.entity.MailTemplateId;
import app.komunumo.domain.event.control.EventService;
import app.komunumo.domain.event.entity.EventDto;
import app.komunumo.domain.event.entity.EventStatus;
import app.komunumo.domain.event.entity.EventVisibility;
import app.komunumo.domain.member.control.MemberService;
import app.komunumo.domain.member.entity.MemberDto;
import app.komunumo.domain.member.entity.MemberRole;
import app.komunumo.domain.page.control.GlobalPageService;
import app.komunumo.domain.page.entity.GlobalPageDto;
import app.komunumo.domain.participant.control.ParticipantService;
import app.komunumo.domain.participant.entity.ParticipantDto;
import app.komunumo.domain.user.control.UserService;
import app.komunumo.domain.user.entity.UserDto;
import app.komunumo.domain.user.entity.UserRole;
import app.komunumo.domain.user.entity.UserType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class JSONExporterTest {

    private static final UUID TEST_UUID_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TEST_UUID_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TEST_UUID_3 = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private ConfigurationService configurationService;
    private ImageService imageService;
    private UserService userService;
    private CommunityService communityService;
    private MemberService memberService;
    private EventService eventService;
    private ParticipantService participantService;
    private GlobalPageService globalPageService;
    private MailService mailService;
    private JSONExporter exporter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        configurationService = mock(ConfigurationService.class);
        imageService = mock(ImageService.class);
        userService = mock(UserService.class);
        communityService = mock(CommunityService.class);
        memberService = mock(MemberService.class);
        eventService = mock(EventService.class);
        participantService = mock(ParticipantService.class);
        globalPageService = mock(GlobalPageService.class);
        mailService = mock(MailService.class);
        exporter = new JSONExporter();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testExportEmptyData() throws Exception {
        // given
        when(configurationService.getAllConfigurations()).thenReturn(List.of());
        when(imageService.getAllImages()).thenReturn(List.of());
        when(userService.getAllUsers()).thenReturn(List.of());
        when(communityService.getCommunities()).thenReturn(List.of());
        when(eventService.getEvents()).thenReturn(List.of());
        when(memberService.getMembers()).thenReturn(List.of());
        when(participantService.getAllParticipants()).thenReturn(List.of());
        when(globalPageService.getAllGlobalPages()).thenReturn(List.of());
        when(mailService.getAllMailTemplates()).thenReturn(List.of());

        // when
        final String json = exporter.exportAll(
                configurationService, imageService, userService, communityService,
                memberService, eventService, participantService, globalPageService, mailService
        );

        // then
        final JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("settings")).isTrue();
        assertThat(root.has("images")).isTrue();
        assertThat(root.has("users")).isTrue();
        assertThat(root.has("communities")).isTrue();
        assertThat(root.has("events")).isTrue();
        assertThat(root.has("members")).isTrue();
        assertThat(root.has("participants")).isTrue();
        assertThat(root.has("globalPages")).isTrue();
        assertThat(root.has("mailTemplates")).isTrue();

        assertThat(root.get("settings")).isEmpty();
        assertThat(root.get("images")).isEmpty();
        assertThat(root.get("users")).isEmpty();
        assertThat(root.get("communities")).isEmpty();
        assertThat(root.get("events")).isEmpty();
        assertThat(root.get("members")).isEmpty();
        assertThat(root.get("participants")).isEmpty();
        assertThat(root.get("globalPages")).isEmpty();
        assertThat(root.get("mailTemplates")).isEmpty();
    }

    @Test
    void testExportSettings() throws Exception {
        // given
        final var config1 = new ConfigurationValue("instance.name", "", "Test Instance");
        final var config2 = new ConfigurationValue("instance.slogan", "EN", "English Slogan");
        when(configurationService.getAllConfigurations()).thenReturn(List.of(config1, config2));
        mockEmptyServices();

        // when
        final String json = exporter.exportAll(
                configurationService, imageService, userService, communityService,
                memberService, eventService, participantService, globalPageService, mailService
        );

        // then
        final JsonNode root = objectMapper.readTree(json);
        final JsonNode settings = root.get("settings");
        assertThat(settings).hasSize(2);
        assertThat(settings.get(0).get("setting").asString()).isEqualTo("instance.name");
        assertThat(settings.get(0).get("language").isNull()).isTrue();
        assertThat(settings.get(0).get("value").asString()).isEqualTo("Test Instance");
        assertThat(settings.get(1).get("setting").asString()).isEqualTo("instance.slogan");
        assertThat(settings.get(1).get("language").asString()).isEqualTo("EN");
    }

    @Test
    void testExportUsers() throws Exception {
        // given
        final var user = new UserDto(TEST_UUID_1, null, null, "testuser", "test@example.com",
                "Test User", "Bio text", TEST_UUID_2, UserRole.USER, UserType.LOCAL);
        when(userService.getAllUsers()).thenReturn(List.of(user));
        mockEmptyServicesExceptUsers();

        // when
        final String json = exporter.exportAll(
                configurationService, imageService, userService, communityService,
                memberService, eventService, participantService, globalPageService, mailService
        );

        // then
        final JsonNode root = objectMapper.readTree(json);
        final JsonNode users = root.get("users");
        assertThat(users).hasSize(1);
        assertThat(users.get(0).get("userId").asString()).isEqualTo(TEST_UUID_1.toString());
        assertThat(users.get(0).get("profile").asString()).isEqualTo("testuser");
        assertThat(users.get(0).get("email").asString()).isEqualTo("test@example.com");
        assertThat(users.get(0).get("name").asString()).isEqualTo("Test User");
        assertThat(users.get(0).get("role").asString()).isEqualTo("USER");
        assertThat(users.get(0).get("type").asString()).isEqualTo("LOCAL");
    }

    @Test
    void testExportCommunities() throws Exception {
        // given
        final var community = new CommunityDto(TEST_UUID_1, "test-community", null, null,
                "Test Community", "A test community description", TEST_UUID_2);
        when(communityService.getCommunities()).thenReturn(List.of(community));
        mockEmptyServicesExceptCommunities();

        // when
        final String json = exporter.exportAll(
                configurationService, imageService, userService, communityService,
                memberService, eventService, participantService, globalPageService, mailService
        );

        // then
        final JsonNode root = objectMapper.readTree(json);
        final JsonNode communities = root.get("communities");
        assertThat(communities).hasSize(1);
        assertThat(communities.get(0).get("communityId").asString()).isEqualTo(TEST_UUID_1.toString());
        assertThat(communities.get(0).get("profile").asString()).isEqualTo("test-community");
        assertThat(communities.get(0).get("name").asString()).isEqualTo("Test Community");
        assertThat(communities.get(0).get("description").asString()).isEqualTo("A test community description");
    }

    @Test
    void testExportEvents() throws Exception {
        // given
        final var begin = ZonedDateTime.now();
        final var end = begin.plusHours(2);
        final var event = new EventDto(TEST_UUID_1, TEST_UUID_2, null, null,
                "Test Event", "Event description", "Test Location",
                begin, end, TEST_UUID_3, EventVisibility.PUBLIC, EventStatus.PUBLISHED);
        when(eventService.getEvents()).thenReturn(List.of(event));
        mockEmptyServicesExceptEvents();

        // when
        final String json = exporter.exportAll(
                configurationService, imageService, userService, communityService,
                memberService, eventService, participantService, globalPageService, mailService
        );

        // then
        final JsonNode root = objectMapper.readTree(json);
        final JsonNode events = root.get("events");
        assertThat(events).hasSize(1);
        assertThat(events.get(0).get("eventId").asString()).isEqualTo(TEST_UUID_1.toString());
        assertThat(events.get(0).get("communityId").asString()).isEqualTo(TEST_UUID_2.toString());
        assertThat(events.get(0).get("title").asString()).isEqualTo("Test Event");
        assertThat(events.get(0).get("visibility").asString()).isEqualTo("PUBLIC");
        assertThat(events.get(0).get("status").asString()).isEqualTo("PUBLISHED");
    }

    @Test
    void testExportMembers() throws Exception {
        // given
        final var since = ZonedDateTime.now();
        final var member = new MemberDto(TEST_UUID_1, TEST_UUID_2, MemberRole.OWNER, since);
        when(memberService.getMembers()).thenReturn(List.of(member));
        mockEmptyServicesExceptMembers();

        // when
        final String json = exporter.exportAll(
                configurationService, imageService, userService, communityService,
                memberService, eventService, participantService, globalPageService, mailService
        );

        // then
        final JsonNode root = objectMapper.readTree(json);
        final JsonNode members = root.get("members");
        assertThat(members).hasSize(1);
        assertThat(members.get(0).get("userId").asString()).isEqualTo(TEST_UUID_1.toString());
        assertThat(members.get(0).get("communityId").asString()).isEqualTo(TEST_UUID_2.toString());
        assertThat(members.get(0).get("role").asString()).isEqualTo("OWNER");
    }

    @Test
    void testExportParticipants() throws Exception {
        // given
        final var registered = ZonedDateTime.now();
        final var participant = new ParticipantDto(TEST_UUID_1, TEST_UUID_2, registered);
        when(participantService.getAllParticipants()).thenReturn(List.of(participant));
        mockEmptyServicesExceptParticipants();

        // when
        final String json = exporter.exportAll(
                configurationService, imageService, userService, communityService,
                memberService, eventService, participantService, globalPageService, mailService
        );

        // then
        final JsonNode root = objectMapper.readTree(json);
        final JsonNode participants = root.get("participants");
        assertThat(participants).hasSize(1);
        assertThat(participants.get(0).get("eventId").asString()).isEqualTo(TEST_UUID_1.toString());
        assertThat(participants.get(0).get("userId").asString()).isEqualTo(TEST_UUID_2.toString());
    }

    @Test
    void testExportGlobalPages() throws Exception {
        // given
        final var page = new GlobalPageDto("about", Locale.ENGLISH, null, null, "About Us", "# About\n\nThis is about us.");
        when(globalPageService.getAllGlobalPages()).thenReturn(List.of(page));
        mockEmptyServicesExceptGlobalPages();

        // when
        final String json = exporter.exportAll(
                configurationService, imageService, userService, communityService,
                memberService, eventService, participantService, globalPageService, mailService
        );

        // then
        final JsonNode root = objectMapper.readTree(json);
        final JsonNode globalPages = root.get("globalPages");
        assertThat(globalPages).hasSize(1);
        assertThat(globalPages.get(0).get("slot").asString()).isEqualTo("about");
        assertThat(globalPages.get(0).get("language").asString()).isEqualTo("en");
        assertThat(globalPages.get(0).get("title").asString()).isEqualTo("About Us");
    }

    @Test
    void testExportMailTemplates() throws Exception {
        // given
        final var template = new MailTemplate(MailTemplateId.CONFIRMATION_PROCESS, Locale.ENGLISH,
                "Confirm your email", "Please confirm your email address.");
        when(mailService.getAllMailTemplates()).thenReturn(List.of(template));
        mockEmptyServicesExceptMailTemplates();

        // when
        final String json = exporter.exportAll(
                configurationService, imageService, userService, communityService,
                memberService, eventService, participantService, globalPageService, mailService
        );

        // then
        final JsonNode root = objectMapper.readTree(json);
        final JsonNode mailTemplates = root.get("mailTemplates");
        assertThat(mailTemplates).hasSize(1);
        assertThat(mailTemplates.get(0).get("mailTemplateId").asString()).isEqualTo("CONFIRMATION_PROCESS");
        assertThat(mailTemplates.get(0).get("language").asString()).isEqualTo("en");
        assertThat(mailTemplates.get(0).get("subject").asString()).isEqualTo("Confirm your email");
    }

    @Test
    void testExportImages() throws Exception {
        // given
        final var image = new ImageDto(TEST_UUID_1, ContentType.IMAGE_JPEG);
        when(imageService.getAllImages()).thenReturn(List.of(image));
        mockEmptyServicesExceptImages();

        // when
        final String json = exporter.exportAll(
                configurationService, imageService, userService, communityService,
                memberService, eventService, participantService, globalPageService, mailService
        );

        // then
        final JsonNode root = objectMapper.readTree(json);
        final JsonNode images = root.get("images");
        assertThat(images).hasSize(1);
        assertThat(images.get(0).get("imageId").asString()).isEqualTo(TEST_UUID_1.toString());
        assertThat(images.get(0).get("contentType").asString()).isEqualTo("image/jpeg");
    }

    private void mockEmptyServices() {
        when(configurationService.getAllConfigurations()).thenReturn(List.of());
        when(imageService.getAllImages()).thenReturn(List.of());
        when(userService.getAllUsers()).thenReturn(List.of());
        when(communityService.getCommunities()).thenReturn(List.of());
        when(eventService.getEvents()).thenReturn(List.of());
        when(memberService.getMembers()).thenReturn(List.of());
        when(participantService.getAllParticipants()).thenReturn(List.of());
        when(globalPageService.getAllGlobalPages()).thenReturn(List.of());
        when(mailService.getAllMailTemplates()).thenReturn(List.of());
    }

    private void mockEmptyServicesExceptUsers() {
        when(configurationService.getAllConfigurations()).thenReturn(List.of());
        when(imageService.getAllImages()).thenReturn(List.of());
        when(communityService.getCommunities()).thenReturn(List.of());
        when(eventService.getEvents()).thenReturn(List.of());
        when(memberService.getMembers()).thenReturn(List.of());
        when(participantService.getAllParticipants()).thenReturn(List.of());
        when(globalPageService.getAllGlobalPages()).thenReturn(List.of());
        when(mailService.getAllMailTemplates()).thenReturn(List.of());
    }

    private void mockEmptyServicesExceptCommunities() {
        when(configurationService.getAllConfigurations()).thenReturn(List.of());
        when(imageService.getAllImages()).thenReturn(List.of());
        when(userService.getAllUsers()).thenReturn(List.of());
        when(eventService.getEvents()).thenReturn(List.of());
        when(memberService.getMembers()).thenReturn(List.of());
        when(participantService.getAllParticipants()).thenReturn(List.of());
        when(globalPageService.getAllGlobalPages()).thenReturn(List.of());
        when(mailService.getAllMailTemplates()).thenReturn(List.of());
    }

    private void mockEmptyServicesExceptEvents() {
        when(configurationService.getAllConfigurations()).thenReturn(List.of());
        when(imageService.getAllImages()).thenReturn(List.of());
        when(userService.getAllUsers()).thenReturn(List.of());
        when(communityService.getCommunities()).thenReturn(List.of());
        when(memberService.getMembers()).thenReturn(List.of());
        when(participantService.getAllParticipants()).thenReturn(List.of());
        when(globalPageService.getAllGlobalPages()).thenReturn(List.of());
        when(mailService.getAllMailTemplates()).thenReturn(List.of());
    }

    private void mockEmptyServicesExceptMembers() {
        when(configurationService.getAllConfigurations()).thenReturn(List.of());
        when(imageService.getAllImages()).thenReturn(List.of());
        when(userService.getAllUsers()).thenReturn(List.of());
        when(communityService.getCommunities()).thenReturn(List.of());
        when(eventService.getEvents()).thenReturn(List.of());
        when(participantService.getAllParticipants()).thenReturn(List.of());
        when(globalPageService.getAllGlobalPages()).thenReturn(List.of());
        when(mailService.getAllMailTemplates()).thenReturn(List.of());
    }

    private void mockEmptyServicesExceptParticipants() {
        when(configurationService.getAllConfigurations()).thenReturn(List.of());
        when(imageService.getAllImages()).thenReturn(List.of());
        when(userService.getAllUsers()).thenReturn(List.of());
        when(communityService.getCommunities()).thenReturn(List.of());
        when(eventService.getEvents()).thenReturn(List.of());
        when(memberService.getMembers()).thenReturn(List.of());
        when(globalPageService.getAllGlobalPages()).thenReturn(List.of());
        when(mailService.getAllMailTemplates()).thenReturn(List.of());
    }

    private void mockEmptyServicesExceptGlobalPages() {
        when(configurationService.getAllConfigurations()).thenReturn(List.of());
        when(imageService.getAllImages()).thenReturn(List.of());
        when(userService.getAllUsers()).thenReturn(List.of());
        when(communityService.getCommunities()).thenReturn(List.of());
        when(eventService.getEvents()).thenReturn(List.of());
        when(memberService.getMembers()).thenReturn(List.of());
        when(participantService.getAllParticipants()).thenReturn(List.of());
        when(mailService.getAllMailTemplates()).thenReturn(List.of());
    }

    private void mockEmptyServicesExceptMailTemplates() {
        when(configurationService.getAllConfigurations()).thenReturn(List.of());
        when(imageService.getAllImages()).thenReturn(List.of());
        when(userService.getAllUsers()).thenReturn(List.of());
        when(communityService.getCommunities()).thenReturn(List.of());
        when(eventService.getEvents()).thenReturn(List.of());
        when(memberService.getMembers()).thenReturn(List.of());
        when(participantService.getAllParticipants()).thenReturn(List.of());
        when(globalPageService.getAllGlobalPages()).thenReturn(List.of());
    }

    private void mockEmptyServicesExceptImages() {
        when(configurationService.getAllConfigurations()).thenReturn(List.of());
        when(userService.getAllUsers()).thenReturn(List.of());
        when(communityService.getCommunities()).thenReturn(List.of());
        when(eventService.getEvents()).thenReturn(List.of());
        when(memberService.getMembers()).thenReturn(List.of());
        when(participantService.getAllParticipants()).thenReturn(List.of());
        when(globalPageService.getAllGlobalPages()).thenReturn(List.of());
        when(mailService.getAllMailTemplates()).thenReturn(List.of());
    }
}
