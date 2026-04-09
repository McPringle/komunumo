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
import org.flywaydb.core.Flyway;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Jdbc;
import org.testcontainers.containers.MariaDBContainer;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JooqCodegenRunner {

    private JooqCodegenRunner() {
    }

    @SuppressWarnings("deprecation")
    public static void main(final String[] args) throws Exception {
        final Path projectDirectory = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        final Path configurationFile = args.length > 1
                ? projectDirectory.resolve(args[1]).normalize()
                : projectDirectory.resolve("src/main/resources/jooq-codegen.xml");
        final Path migrationsDirectory = projectDirectory.resolve("src/main/resources/db/migration");
        final Path jnaTempDirectory = projectDirectory.resolve("target/tmp/jna");

        Files.createDirectories(jnaTempDirectory);
        System.setProperty("jna.tmpdir", jnaTempDirectory.toString());

        try (MariaDBContainer<?> container = new MariaDBContainer<>("mariadb:lts")
                .withDatabaseName("jooq")
                .withUsername("test")
                .withPassword("test")) {
            container.start();

            Flyway.configure()
                    .dataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword())
                    .locations("filesystem:" + migrationsDirectory)
                    .placeholderReplacement(false)
                    .load()
                    .migrate();

            final Configuration configuration;
            try (InputStream inputStream = Files.newInputStream(configurationFile)) {
                configuration = GenerationTool.load(inputStream);
            }

            configuration.setBasedir(projectDirectory.toString());

            Jdbc jdbc = configuration.getJdbc();
            if (jdbc == null) {
                jdbc = new Jdbc();
                configuration.setJdbc(jdbc);
            }

            jdbc.setDriver(container.getDriverClassName());
            jdbc.setUrl(container.getJdbcUrl());
            jdbc.setUser(container.getUsername());
            jdbc.setUsername(container.getUsername());
            jdbc.setPassword(container.getPassword());
            jdbc.setInitScript(null);

            GenerationTool.generate(configuration);
        }
    }
}
