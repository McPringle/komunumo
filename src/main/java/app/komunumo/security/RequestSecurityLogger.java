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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@Order(1)
public final class RequestSecurityLogger extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestSecurityLogger.class);

  @Override
  protected void doFilterInternal(final @NotNull HttpServletRequest request,
                                  final @NotNull HttpServletResponse response,
                                  final @NotNull FilterChain filterChain)
      throws ServletException, IOException {

    final var sessionId = (request.getSession(false) != null) ? request.getSession(false).getId() : "no-session";
    final var auth = SecurityContextHolder.getContext().getAuthentication();
    final var user = (auth != null) ? String.valueOf(auth.getPrincipal()) : "anonymous";
    final var roles = (auth != null && auth.getAuthorities() != null)
        ? auth.getAuthorities().stream().map(Object::toString).collect(Collectors.joining(","))
        : "-";

    final var cookies = (request.getCookies() != null)
        ? Arrays.stream(request.getCookies()).map(c -> c.getName() + "=" + c.getValue())
            .collect(Collectors.joining(";"))
        : "no-cookies";

    LOGGER.info("SECURITY TRACE -> {} {} | session={} | user={} | roles={} | cookies={}",
        request.getMethod(), request.getRequestURI(), sessionId, user, roles, cookies);

    filterChain.doFilter(request, response);
  }

}
