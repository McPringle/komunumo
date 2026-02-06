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

import app.komunumo.domain.core.config.entity.AppConfig;
import app.komunumo.domain.core.image.boundary.ImageServlet;
import app.komunumo.domain.core.image.control.ImageService;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;
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
@StyleSheet("css/styles.css")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes, viewport-fit=cover")
@PWA(name = "Komunumo - Open Source Community Manager", shortName = "Komunumo")
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {

    private final @NotNull AppConfig appConfig;

    /**
     * <p>Creates the main application instance.</p>
     *
     * <p>The application configuration is injected by Spring Boot and stored for later use during application startup
     * and runtime, for example when configuring the Vaadin application shell or registering infrastructure components
     * that depend on file system paths.</p>
     *
     * @param appConfig the application configuration providing access to runtime and file system settings
     */
    public Application(final @NotNull AppConfig appConfig) {
        super();
        this.appConfig = appConfig;
    }

    /**
     * <p>Starts the Spring Boot application.</p>
     *
     * <p>This is the main entry point when the application is launched as a standalone JVM process. It bootstraps the
     * Spring application context, triggers component scanning, applies autoconfiguration, and starts the embedded
     * servlet container.</p>
     *
     * @param args the command-line arguments passed to the application
     */
    public static void main(final @NotNull String... args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * <p>Configures the initial page settings for the application shell.</p>
     *
     * <p>This method is invoked by Vaadin at application startup and can be used
     * to customize the HTML page generated for the UI, for example by adding
     * meta tags, favicons, or viewport settings.</p>
     *
     * <p>In this implementation, an optional external stylesheet is added if
     * configured in the application settings. A timestamp query parameter is
     * appended to the stylesheet URL to prevent client-side caching issues.</p>
     *
     * @param settings the {@link AppShellSettings} used to configure the page,
     *                 never {@code null}
     */
    @Override
    public void configurePage(final @NotNull AppShellSettings settings) {
        final var baseDir = appConfig.files().basedir();
        final var stylePath = baseDir.resolve("custom", "styles", "styles.css");

        if (stylePath.toFile().exists()) {
            settings.addLink("stylesheet", "/custom/styles/styles.css");
        }
    }

    /**
     * <p>Registers the {@link ImageServlet} to handle HTTP requests to {@code /images/*}.</p>
     *
     * <p>This servlet is responsible for streaming stored image files from the file system
     * and serves images with appropriate cache headers.</p>
     *
     * @param imageService the image service used to retrieve image data
     * @return a servlet registration bean that maps {@code /images/*} to {@link ImageServlet}
     */
    @Bean
    public @NotNull ServletRegistrationBean<@NotNull HttpServlet> imageServlet(
            final @NotNull ImageService imageService) {
        return new ServletRegistrationBean<>(
                new ImageServlet(appConfig, imageService),
                "/images/*"
        );
    }

}
