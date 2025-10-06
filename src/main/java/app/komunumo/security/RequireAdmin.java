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

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Composed security annotation that requires the caller to have the {@code ADMIN} role.</p>
 *
 * <p>Under Spring Security's default role prefixing, {@code hasRole('ADMIN')} checks for the
 * {@code ROLE_ADMIN} authority on the current {@link org.springframework.security.core.Authentication}.</p>
 *
 * <p>This annotation can be placed on a type to guard all its public methods, or on individual
 * methods to enforce access locally. It relies on Spring Method Security being enabled via
 * {@link org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity}.</p>
 *
 * <p>When renaming roles or customizing the role prefix (e.g., via
 * {@link org.springframework.security.config.core.GrantedAuthorityDefaults}), update this annotation
 * accordingly to keep the policy consistent.</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('ADMIN')")
public @interface RequireAdmin { }
