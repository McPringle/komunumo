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
package app.komunumo.domain.core.exporter.boundary;

import app.komunumo.domain.community.control.CommunityService;
import app.komunumo.domain.core.config.control.ConfigurationService;
import app.komunumo.domain.core.exporter.control.JSONExporter;
import app.komunumo.domain.core.image.control.ImageService;
import app.komunumo.domain.core.layout.boundary.WebsiteLayout;
import app.komunumo.domain.core.mail.control.MailService;
import app.komunumo.domain.event.control.EventService;
import app.komunumo.domain.member.control.MemberService;
import app.komunumo.domain.page.control.GlobalPageService;
import app.komunumo.domain.participant.control.ParticipantService;
import app.komunumo.domain.user.control.UserService;
import app.komunumo.vaadin.components.AbstractView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RolesAllowed("ADMIN")
@Route(value = "admin/export", layout = WebsiteLayout.class)
public final class ExporterView extends AbstractView {

    private final @NotNull ConfigurationService configurationService;
    private final @NotNull ImageService imageService;
    private final @NotNull UserService userService;
    private final @NotNull CommunityService communityService;
    private final @NotNull MemberService memberService;
    private final @NotNull EventService eventService;
    private final @NotNull ParticipantService participantService;
    private final @NotNull GlobalPageService globalPageService;
    private final @NotNull MailService mailService;

    private final @NotNull UI ui;
    private final @NotNull Button exportButton;
    private final @NotNull UnorderedList exportLog;
    private final @NotNull VerticalLayout exportFieldsContainer;
    private final @NotNull VerticalLayout exportLogContainer;

    @SuppressWarnings("checkstyle:ParameterNumber") // constructor injection
    public ExporterView(final @NotNull ConfigurationService configurationService,
                        final @NotNull ImageService imageService,
                        final @NotNull UserService userService,
                        final @NotNull CommunityService communityService,
                        final @NotNull MemberService memberService,
                        final @NotNull EventService eventService,
                        final @NotNull ParticipantService participantService,
                        final @NotNull GlobalPageService globalPageService,
                        final @NotNull MailService mailService) {
        super(configurationService);

        this.configurationService = configurationService;
        this.imageService = imageService;
        this.userService = userService;
        this.communityService = communityService;
        this.memberService = memberService;
        this.eventService = eventService;
        this.participantService = participantService;
        this.globalPageService = globalPageService;
        this.mailService = mailService;
        this.ui = UI.getCurrent();

        exportButton = new Button(getTranslation("core.exporter.boundary.ExporterView.startExportButton"));
        exportButton.setEnabled(true);
        exportButton.addClassName("start-export-button");
        exportButton.addClickListener(_ -> processExport());

        final var exportLogTitle = new H3(getTranslation("core.exporter.boundary.ExporterView.exportLogTitle"));
        exportLog = new UnorderedList();
        exportLog.addClassName("export-log");

        addClassName("exporter-view");
        add(new H2(getTranslation("core.exporter.boundary.ExporterView.title")));

        exportFieldsContainer = new VerticalLayout();
        exportFieldsContainer.setId("export-fields-container");
        exportFieldsContainer.add(exportButton);
        add(exportFieldsContainer);

        exportLogContainer = new VerticalLayout();
        exportLogContainer.setId("export-log-container");
        exportLogContainer.add(exportLogTitle);
        exportLogContainer.add(exportLog);
        exportLogContainer.setVisible(false);
        add(exportLogContainer);
    }

    private void processExport() {
        exportFieldsContainer.setEnabled(false);
        exportLogContainer.setVisible(true);
        exportLog.add(new ListItem(getTranslation("core.exporter.boundary.ExporterView.exportStarted")));
        ui.access(() -> {
            try {
                final var jsonExporter = new JSONExporter();
                final String json = jsonExporter.exportAll(
                        configurationService,
                        imageService,
                        userService,
                        communityService,
                        memberService,
                        eventService,
                        participantService,
                        globalPageService,
                        mailService
                );

                final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                final String fileName = "komunumo-export-" + timestamp + ".json";

                final StreamResource resource = new StreamResource(fileName,
                        () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
                resource.setContentType("application/json");
                resource.setCacheTime(0);

                final Anchor downloadLink = new Anchor(resource,
                        getTranslation("core.exporter.boundary.ExporterView.downloadLink"));
                downloadLink.getElement().setAttribute("download", true);
                downloadLink.addClassName("export-download-link");
                exportFieldsContainer.add(downloadLink);

                exportLog.add(new ListItem(getTranslation("core.exporter.boundary.ExporterView.exportSuccess")));
            } catch (final Exception e) {
                exportLog.add(new ListItem(getTranslation("core.exporter.boundary.ExporterView.exportFailed")
                        + ": " + e.getMessage()));
                Notification.show(getTranslation("core.exporter.boundary.ExporterView.exportFailed"),
                        5000, Notification.Position.MIDDLE);
            } finally {
                exportFieldsContainer.setEnabled(true);
            }
        });
    }

    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation("core.exporter.boundary.ExporterView.title");
    }

}
