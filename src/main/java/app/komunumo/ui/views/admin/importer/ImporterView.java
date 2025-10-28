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
package app.komunumo.ui.views.admin.importer;

import app.komunumo.data.importer.JSONImporter;
import app.komunumo.data.service.CommunityService;
import app.komunumo.data.service.ConfigurationService;
import app.komunumo.data.service.EventService;
import app.komunumo.data.service.GlobalPageService;
import app.komunumo.data.service.ImageService;
import app.komunumo.data.service.UserService;
import app.komunumo.ui.components.AbstractView;
import app.komunumo.ui.views.WebsiteLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@RolesAllowed("ADMIN")
@Route(value = "admin/import", layout = WebsiteLayout.class)
public final class ImporterView extends AbstractView {

    private final @NotNull ConfigurationService configurationService;
    private final @NotNull ImageService imageService;
    private final @NotNull UserService userService;
    private final @NotNull CommunityService communityService;
    private final @NotNull EventService eventService;
    private final @NotNull GlobalPageService globalPageService;

    private final @NotNull TextField urlField;
    private final @NotNull Button importButton;
    private final @NotNull H3 importStatusTitle;
    private final @NotNull UnorderedList importStatus;

    public ImporterView(final @NotNull ConfigurationService configurationService,
                        final @NotNull ImageService imageService,
                        final @NotNull UserService userService,
                        final @NotNull CommunityService communityService,
                        final @NotNull EventService eventService,
                        final @NotNull GlobalPageService globalPageService) {
        super(configurationService);

        this.configurationService = configurationService;
        this.imageService = imageService;
        this.userService = userService;
        this.communityService = communityService;
        this.eventService = eventService;
        this.globalPageService = globalPageService;

        addClassName("importer-view");
        add(new H2(getTranslation("ui.views.admin.importer.ImporterView.title")));

        urlField = new TextField();
        urlField.setValueChangeMode(ValueChangeMode.EAGER);
        urlField.setPlaceholder(getTranslation("ui.views.admin.importer.ImporterView.urlFieldPlaceholder"));
        urlField.setWidthFull();
        urlField.addClassName("url-field");
        add(urlField);

        importButton = new Button(getTranslation("ui.views.admin.importer.ImporterView.startImportButton"));
        importButton.setEnabled(false);
        importButton.addClassName("start-import-button");
        add(importButton);

        urlField.addValueChangeListener(valueChangeEvent ->
                importButton.setEnabled(!valueChangeEvent.getValue().isBlank()));

        importStatusTitle = new H3(getTranslation("ui.views.admin.importer.ImporterView.importStatus"));
        importStatusTitle.setVisible(false);
        add(importStatusTitle);

        importStatus = new UnorderedList();
        importStatus.addClassName("import-status");
        add(importStatus);

        importButton.addClickListener(_ -> processImport());
    }

    private void processImport() {
        importButton.setEnabled(false);
        urlField.setEnabled(false);
        importStatusTitle.setVisible(true);
        importStatus.removeAll();
        importStatus.add(new ListItem(getTranslation("ui.views.admin.importer.ImporterView.status.importStatus")));

        final var ui = UI.getCurrent();
        final var jsonDataUrl = urlField.getValue();
        CompletableFuture
                .runAsync(() -> {
                    final var jsonImporter = new JSONImporter(jsonDataUrl);

                    ui.access(() -> importStatus.add(new ListItem(
                            getTranslation("ui.views.admin.importer.ImporterView.status.importSettings"))));
                    jsonImporter.importSettings(configurationService);

                    ui.access(() -> importStatus.add(new ListItem(
                            getTranslation("ui.views.admin.importer.ImporterView.status.importImages"))));
                    jsonImporter.importImages(imageService);

                    ui.access(() -> importStatus.add(new ListItem(
                            getTranslation("ui.views.admin.importer.ImporterView.status.importUsers"))));
                    jsonImporter.importUsers(userService);

                    ui.access(() -> importStatus.add(new ListItem(
                            getTranslation("ui.views.admin.importer.ImporterView.status.importCommunities"))));
                    jsonImporter.importCommunities(communityService);

                    ui.access(() -> importStatus.add(new ListItem(
                            getTranslation("ui.views.admin.importer.ImporterView.status.importEvents"))));
                    jsonImporter.importEvents(eventService);

                    ui.access(() -> importStatus.add(new ListItem(
                            getTranslation("ui.views.admin.importer.ImporterView.status.importGlobalPages"))));
                    jsonImporter.importGlobalPages(globalPageService);
                })
                .thenRunAsync(() -> ui.access(() -> {
                    importStatus.add(new ListItem(
                            getTranslation("ui.views.admin.importer.ImporterView.status.importFinished")));
                    importButton.setEnabled(true);
                    urlField.setEnabled(true);
                }))
                .exceptionally(exception -> {
                    ui.access(() -> {
                        importStatus.add(new ListItem(exception.getCause().getMessage()));
                        importButton.setEnabled(true);
                        urlField.setEnabled(true);
                    });
                    return null;
                });
    }

    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation("ui.views.admin.importer.ImporterView.title");
    }

}
