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
package app.komunumo.domain.core.importer.boundary;

import app.komunumo.domain.core.importer.control.ImporterLog;
import app.komunumo.domain.core.importer.control.JSONImporter;
import app.komunumo.domain.community.control.CommunityService;
import app.komunumo.domain.core.config.control.ConfigurationService;
import app.komunumo.domain.event.control.EventService;
import app.komunumo.domain.page.control.GlobalPageService;
import app.komunumo.domain.core.image.control.ImageService;
import app.komunumo.data.service.MemberService;
import app.komunumo.domain.user.control.UserService;
import app.komunumo.ui.components.AbstractView;
import app.komunumo.domain.core.layout.boundary.WebsiteLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.server.streams.UploadMetadata;
import jakarta.annotation.security.RolesAllowed;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.CompletableFuture;

@RolesAllowed("ADMIN")
@Route(value = "admin/import", layout = WebsiteLayout.class)
public final class ImporterView extends AbstractView {

    private final @NotNull ConfigurationService configurationService;
    private final @NotNull ImageService imageService;
    private final @NotNull UserService userService;
    private final @NotNull CommunityService communityService;
    private final @NotNull MemberService memberService;
    private final @NotNull EventService eventService;
    private final @NotNull GlobalPageService globalPageService;

    private final @NotNull UI ui;
    private final @NotNull TextField urlField;
    private final @NotNull Button importButton;
    private final @NotNull UnorderedList importLog;

    private final @NotNull VerticalLayout importFieldsContainer;
    private final @NotNull VerticalLayout importLogContainer;

    private File uploadFile = null;

    public ImporterView(final @NotNull ConfigurationService configurationService,
                        final @NotNull ImageService imageService,
                        final @NotNull UserService userService,
                        final @NotNull CommunityService communityService,
                        final @NotNull MemberService memberService,
                        final @NotNull EventService eventService,
                        final @NotNull GlobalPageService globalPageService) {
        super(configurationService);

        this.configurationService = configurationService;
        this.imageService = imageService;
        this.userService = userService;
        this.communityService = communityService;
        this.memberService = memberService;
        this.eventService = eventService;
        this.globalPageService = globalPageService;
        this.ui = UI.getCurrent();

        final var uploadHandler = UploadHandler
                .toTempFile(this::processUploadSuccess);

        final var uploadI18N = new UploadI18N();
        uploadI18N.setAddFiles(new UploadI18N.AddFiles().setOne(
                getTranslation("ui.views.admin.importer.ImporterView.uploadButton")));
        uploadI18N.setDropFiles(new UploadI18N.DropFiles().setOne(
                getTranslation("ui.views.admin.importer.ImporterView.uploadDrop")));
        uploadI18N.setError(new UploadI18N.Error().setIncorrectFileType(
                getTranslation("ui.views.admin.importer.ImporterView.uploadIncorrectFileType")));

        final var uploadField = new Upload(uploadHandler);
        uploadField.setSizeFull();
        uploadField.setMaxFiles(1);
        uploadField.setAcceptedFileTypes("application/json", ".json");
        uploadField.setI18n(uploadI18N);

        urlField = new TextField();
        urlField.setValueChangeMode(ValueChangeMode.EAGER);
        urlField.setPlaceholder(getTranslation("ui.views.admin.importer.ImporterView.urlFieldPlaceholder"));
        urlField.setWidthFull();
        urlField.addClassName("url-field");

        importButton = new Button(getTranslation("ui.views.admin.importer.ImporterView.startImportButton"));
        importButton.setEnabled(false);
        importButton.addClassName("start-import-button");
        importButton.addClickListener(_ -> processImport());

        final var importLogTitle = new H3(getTranslation("ui.views.admin.importer.ImporterView.importLogTitle"));
        importLog = new UnorderedList();
        importLog.addClassName("import-log");

        urlField.addValueChangeListener(valueChangeEvent ->
                importButton.setEnabled(!valueChangeEvent.getValue().isBlank()));

        addClassName("importer-view");
        add(new H2(getTranslation("ui.views.admin.importer.ImporterView.title")));

        importFieldsContainer = new VerticalLayout();
        importFieldsContainer.setId("import-fields-container");
        importFieldsContainer.add(uploadField);
        importFieldsContainer.add(urlField);
        importFieldsContainer.add(importButton);
        add(importFieldsContainer);

        importLogContainer = new VerticalLayout();
        importLogContainer.setId("import-log-container");
        importLogContainer.add(importLogTitle);
        importLogContainer.add(importLog);
        importLogContainer.setVisible(false);
        add(importLogContainer);
    }

    private void processUploadSuccess(final @NotNull UploadMetadata metadata, final @NotNull File file) {
        file.deleteOnExit();
        uploadFile = file;
        processImport();
    }

    private void processImport() {
        importFieldsContainer.setEnabled(false);
        importLogContainer.setVisible(true);
        final var importerLog = new ImporterLog((message) ->
                ui.access(() -> importLog.add(new ListItem(message))));
        CompletableFuture
                .runAsync(() -> {
                    final var jsonDataUrl = urlField.getValue();
                    final var jsonImporter = uploadFile == null
                            ? new JSONImporter(importerLog, jsonDataUrl)
                            : new JSONImporter(importerLog, uploadFile);

                    jsonImporter.importSettings(configurationService);
                    jsonImporter.importImages(imageService);
                    jsonImporter.importUsers(userService);
                    jsonImporter.importCommunities(communityService);
                    jsonImporter.importMembers(memberService);
                    jsonImporter.importEvents(eventService);
                    jsonImporter.importGlobalPages(globalPageService);
                })
                .thenRunAsync(() -> ui.access(() -> {
                    if (uploadFile != null) {
                        //noinspection ResultOfMethodCallIgnored
                        uploadFile.delete();
                        uploadFile = null;
                    }
                    importFieldsContainer.setEnabled(true);
                }))
                .exceptionally(_ -> {
                    ui.access(() -> importFieldsContainer.setEnabled(true));
                    return null;
                });
    }

    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation("ui.views.admin.importer.ImporterView.title");
    }

}
