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
package app.komunumo.ui.views.community;

import app.komunumo.data.dto.CommunityDto;
import app.komunumo.data.dto.MemberDto;
import app.komunumo.data.dto.MemberRole;
import app.komunumo.data.service.CommunityService;
import app.komunumo.data.service.MemberService;
import app.komunumo.util.SecurityUtil;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.NotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Objects;

public class CreateCommunityComponent extends VerticalLayout {

    private final @NotNull CommunityService communityService;
    private final @NotNull MemberService memberService;

    private final Binder<CommunityDto> binder = new Binder<>(CommunityDto.class);

    CreateCommunityComponent(final @NotNull CommunityService communityService,
                             final @NotNull MemberService memberService) {
        super();
        this.communityService = communityService;
        this.memberService = memberService;

        addClassName("community-add-view");
        add(createAddCommunityComponent());
    }

    private @NotNull Component createAddCommunityComponent() {
        final var profileField = new TextField();
        profileField.setClassName("profile-field");
        profileField.setValueChangeMode(ValueChangeMode.EAGER);
        profileField.setLabel(getTranslation("ui.views.community.CreateCommunityView.label.profile"));
        profileField.setMaxLength(255);
        profileField.setRequired(true);
        profileField.setWidthFull();

        final var nameField = new TextField();
        nameField.setClassName("name-field");
        nameField.setValueChangeMode(ValueChangeMode.EAGER);
        nameField.setLabel(getTranslation("ui.views.community.CreateCommunityView.label.name"));
        nameField.setRequired(true);
        nameField.setMaxLength(255);
        nameField.setWidthFull();

        final var descriptionField = new TextArea();
        descriptionField.setClassName("description-field");
        descriptionField.setValueChangeMode(ValueChangeMode.EAGER);
        descriptionField.setLabel(getTranslation("ui.views.community.CreateCommunityView.label.description"));
        descriptionField.setWidthFull();

        binder.forField(profileField)
                .asRequired(getTranslation("ui.views.community.CreateCommunityView.validation.profile.required"))
                .withValidator(communityService::isProfileNameAvailable,
                        getTranslation("ui.views.community.CreateCommunityView.validation.profile.exists"))
                .bind(CommunityDto::profile, null);

        binder.forField(nameField)
                .asRequired(getTranslation("ui.views.community.CreateCommunityView.validation.name.required"))
                .bind(CommunityDto::name, null);

        Button createButton = new Button(getTranslation("ui.views.community.CreateCommunityView.createButton"), _ -> {
            var userPrincipalOptional = SecurityUtil.getUserPrincipal();

            if (userPrincipalOptional.isEmpty()) {
                showNotification("ui.views.community.CreateCommunityView.permissionError", NotificationVariant.LUMO_ERROR);
            } else if (binder.validate().isOk()) {
                var communityDto = new CommunityDto(null, profileField.getValue(), null, null,
                        nameField.getValue(), descriptionField.getValue(), null);

                communityDto = communityService.storeCommunity(communityDto);

                var userPrincipal = userPrincipalOptional.orElseThrow(NotFoundException::new);
                var memberDto = new MemberDto(userPrincipal.getUserId(),
                        Objects.requireNonNull(communityDto.id()), MemberRole.OWNER, null);

                memberService.storeMember(memberDto);

                showNotification("ui.views.community.CreateCommunityView.notification.success", NotificationVariant.LUMO_SUCCESS);

                UI.getCurrent().navigate("communities/%s".formatted(communityDto.profile()));

            } else {
                showNotification("ui.views.community.CreateCommunityView.notification.error", NotificationVariant.LUMO_ERROR);
            }
        });

        createButton.setClassName("create-button");

        final var container = new VerticalLayout(profileField, nameField, descriptionField, createButton);
        container.addClassName("community-add-component");

        return container;
    }

    /**
     * <p>Utility method to show notification, given the messageKey to display as the message and NotificationVariant
     * to define the type.</p>
     *
     * @param messageKey          the message key that will be shown in the notification
     * @param notificationVariant the notification variant that will define the theme of the notification
     */
    private void showNotification(final @NotNull String messageKey, final @NotNull NotificationVariant notificationVariant) {
        final var notification = new Notification(new Div(getTranslation(messageKey)));
        notification.addThemeVariants(notificationVariant);
        notification.setDuration(10_000);
        notification.open();
    }

    /**
     * <p>Getter method to retrieve the binder object for this component.</p>
     *
     * @return the binder object
     */
    @VisibleForTesting
    public Binder<CommunityDto> getBinder() {
        return binder;
    }
}
