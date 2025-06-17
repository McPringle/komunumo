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
package app.komunumo;

import app.komunumo.configuration.AppConfig;
import app.komunumo.data.service.ImageService;
import app.komunumo.ui.servlets.ImageServlet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import jakarta.servlet.http.HttpServlet;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The entry point of the Spring Boot application.
 */
@Push
@Theme(value = "komunumo")
@PWA(name = "Komunumo - Open Source Community Manager", shortName = "Komunumo")
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {

    public static void main(final @NotNull String... args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * <p>Registers the {@link ImageServlet} to handle HTTP requests to {@code /images/*}.</p>
     *
     * <p>This servlet is responsible for streaming stored image files from the file system
     * and serves images with appropriate cache headers.</p>
     *
     * @param imageService the service used to retrieve image metadata
     * @return a servlet registration bean that maps {@code /images/*} to {@link ImageServlet}
     */
    @Bean
    public @NotNull ServletRegistrationBean<@NotNull HttpServlet> imageServlet(
            final @NotNull ImageService imageService) {
        return new ServletRegistrationBean<>(
                new ImageServlet(imageService),
                "/images/*"
        );
    }

}
