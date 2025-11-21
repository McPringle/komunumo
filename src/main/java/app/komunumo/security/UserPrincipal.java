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
package app.komunumo.security;

import app.komunumo.domain.user.entity.UserDto;
import app.komunumo.domain.user.entity.UserType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class UserPrincipal implements UserDetails {

    private final @NotNull UUID userId;
    private final @NotNull String email;
    private final @NotNull String name;
    private final @NotNull UserType type;
    private final @NotNull Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(final @NotNull UserDto user, final @NotNull List<@NotNull GrantedAuthority> authorities) {
        assert user.id() != null;
        assert user.email() != null;

        this.userId = user.id();
        this.email = user.email();
        this.name = user.name();
        this.type = user.type();
        this.authorities = authorities;
    }

    public @NotNull UUID getUserId() {
        return userId;
    }

    public @NotNull String getEmail() {
        return email;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull UserType getType() {
        return type;
    }

    @Override
    public @NotNull Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return null;
    }

    @Override
    public @NotNull String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
