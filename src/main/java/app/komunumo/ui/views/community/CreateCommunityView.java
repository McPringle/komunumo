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

import app.komunumo.data.service.CommunityService;
import app.komunumo.data.service.ConfigurationService;
import app.komunumo.data.service.MemberService;
import app.komunumo.ui.components.AbstractView;
import app.komunumo.ui.views.WebsiteLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.jetbrains.annotations.NotNull;

@RolesAllowed("USER")
@Route(value = "/communities/new", layout = WebsiteLayout.class)
public class CreateCommunityView extends AbstractView {

    public CreateCommunityView(final @NotNull ConfigurationService configurationService,
                               final @NotNull CommunityService communityService,
                               final @NotNull MemberService memberService) {
        super(configurationService);

        add(new H2(getTranslation("ui.views.community.CreateCommunityView.title")));
        add(new CreateCommunityComponent(communityService, memberService));
    }

    /**
     * <p>The fragment of the page title that will appear before the instance name, separated by a hyphen.</p>
     *
     * @return the translated page title value, based on locale
     */
    @Override
    protected @NotNull String getViewTitle() {
        return getTranslation("ui.views.community.CreateCommunityView.title");
    }

}
