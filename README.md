# Komunumo

[![All Tests](https://github.com/McPringle/komunumo/actions/workflows/all-tests.yml/badge.svg)](https://github.com/McPringle/komunumo/actions/workflows/all-tests.yml)
[![codecov](https://codecov.io/github/McPringle/komunumo/graph/badge.svg?token=6MTYWYK083)](https://codecov.io/github/McPringle/komunumo)

**Open Source Community Manager**

*Komunumo* is an esperanto noun with a meaning of *community*.

## About

You are are community leader? *Komunumo* makes your life a lot easier! Eveything you need to run your community in one place, for free! *Komunumo* is an open source community management software which assists you in all tasks managing a community like a user group or a meetup.

With *Komunumo* you can manage your community members (including self-management), organize events (including registration), send out newsletters, manage your sponsors, and a lot more.

We have set ourselves the goal of implementing all important [Meetup](https://www.meetup.com/) functions for communities in *Komunumo*. We strive for full compatibility with the [Fediverse](https://en.wikipedia.org/wiki/Fediverse) to ensure smooth data exchange with [Mastodon](https://en.wikipedia.org/wiki/Mastodon_(social_network)), [Mobilizon](https://en.wikipedia.org/wiki/Mobilizon), and other services using the [ActivityPub](https://en.wikipedia.org/wiki/ActivityPub) protocol.

## Contributing

You can find a lot of information on how you can contribute to *Komunumo* in the separate file [CONTRIBUTING.md](CONTRIBUTING.md). A curated list of contributors is available in the file [CONTRIBUTORS.md](CONTRIBUTORS.md).

## Communication

### Matrix Chat

There is a channel at Matrix for quick and easy communication. This is publicly accessible for everyone. For developers as well as users. The communication in this chat is to be regarded as short-lived and has no documentary character.

You can find our Matrix channel here: [@komunumo:ijug.eu](https://matrix.to/#/%23komunumo:ijug.eu)

### GitHub Discussions

We use the corresponding GitHub function for discussions. The discussions held here are long-lived and divided into categories for the sake of clarity. One important category, for example, is that for questions and answers.

Discussions on GitHub: https://github.com/McPringle/komunumo/discussions  
Questions and Answers: https://github.com/McPringle/komunumo/discussions/categories/q-a

## Configuration

The file `application.properties` contains only some default values. To override the default values and to specify other configuration options, just set them as environment variables. The following sections describe all available configuration options. You only need to specify these options if your configuration settings differ from the defaults.

### Server

The server runs on port 8080 by default. If you don't like it, change it using an environment variable:

```
KOMUNUMO_PORT=8080
```

### Mail Configuration

*Komunumo* supports sending email notifications. Configuration is done via environment variables using the `KOMUNUMO_MAIL_*` naming scheme.

#### Available Environment Variables

| Variable                          | Default             | Description                                                           |
|-----------------------------------|---------------------|-----------------------------------------------------------------------|
| `KOMUNUMO_MAIL_FROM`              | `noreply@localhost` | Sender address shown in outgoing emails (e.g. noreply@example.com).   |
| `KOMUNUMO_MAIL_REPLY_TO`          | *(empty)*           | Optional reply-to address (e.g. `support@example.com`).               |
| `KOMUNUMO_MAIL_HOST`              | `localhost`         | Mail server address. Use a local MTA or external SMTP provider.       |
| `KOMUNUMO_MAIL_PORT`              | `25`                | Port for the SMTP server (e.g., `587` for STARTTLS or `465` for SSL). |
| `KOMUNUMO_MAIL_PROTOCOL`          | `smtp`              | Protocol used for sending email. Usually `smtp`.                      |
| `KOMUNUMO_MAIL_USERNAME`          | *(empty)*           | Username for SMTP authentication, if required.                        |
| `KOMUNUMO_MAIL_PASSWORD`          | *(empty)*           | Password for SMTP authentication, if required.                        |
| `KOMUNUMO_MAIL_SMTP_AUTH`         | `false`             | Whether SMTP authentication is enabled.                               |
| `KOMUNUMO_MAIL_STARTTLS_ENABLE`   | `false`             | Enable STARTTLS encryption (recommended for port 587).                |
| `KOMUNUMO_MAIL_STARTTLS_REQUIRED` | `false`             | Require STARTTLS (connection will fail if not supported).             |
| `KOMUNUMO_MAIL_SSL_ENABLE`        | `false`             | Enable SSL encryption (typically for port 465).                       |
| `KOMUNUMO_MAIL_ENCODING`          | `UTF-8`             | Default encoding for email subject and content.                       |

#### Example Configuration

In a `.env` file, CI system, or Docker environment:

```bash
KOMUNUMO_MAIL_FROM=noreply@example.com
KOMUNUMO_MAIL_REPLY_TO=support@example.com
KOMUNUMO_MAIL_HOST=smtp.example.com
KOMUNUMO_MAIL_PORT=587
KOMUNUMO_MAIL_USERNAME=myuser
KOMUNUMO_MAIL_PASSWORD=secret
KOMUNUMO_MAIL_SMTP_AUTH=true
KOMUNUMO_MAIL_STARTTLS_ENABLE=true
```

> [!TIP]
> If you are using a local mail relay (e.g., [Nullmailer](https://untroubled.org/nullmailer/) or [Postfix](https://www.postfix.org/)), you can often omit authentication and encryption settings.

### Database

*Komunumo* needs a database to store the business data. By default, *Komunumo* comes with [MariaDB](https://mariadb.org/) drivers. MariaDB is recommended because we are using it during development, and it is highly tested with *Komunumo*. All free and open source JDBC compatible databases are supported, but you need to configure the JDBC driver dependencies accordingly. Please make sure that your database is using a Unicode character set to avoid problems storing data containing Unicode characters. The database user to access the *Komunumo* database executes automatic schema migrations and needs `ALL PRIVILEGES`.

Please configure the database connection using the following environment variables:

```
KOMUNUMO_DB_URL=jdbc:mariadb://localhost:3306/komunumo?serverTimezone\=Europe/Zurich&allowMultiQueries=true
KOMUNUMO_DB_USER=johndoe
KOMUNUMO_DB_PASS=verysecret
```

The database schema will be migrated automatically by *Komunumo*.

#### Important MySQL and MariaDB configuration

MySQL and MariaDB have a possible silent truncation problem with the `GROUP_CONCAT` command. To avoid this it is necessary, to configure these two databases to allow multi queries. Just add `allowMultiQueries=true` to the JDBC database URL like in this example (you may need to scroll the example code to the right):

```
KOMUNUMO_DB_URL=jdbc:mariadb://localhost:3306/komunumo?serverTimezone\=Europe/Zurich&allowMultiQueries=true
```

## Plugin System

*Komunumo* supports a basic plugin mechanism for extending application behavior during startup. To implement a plugin, create a class that implements the `KomunumoPlugin` interface:

```java
public class MyPlugin implements KomunumoPlugin {
    @Override
    public void onApplicationStarted(final @NotNull PluginContext context) {
        final var log = context.getLogger(MyPlugin.class);
        final var services = context.getServiceProvider();
        log.info("MyPlugin initialized!");
        // Use services to interact with the application
    }
}
```

The `onApplicationStarted()` method is invoked after *Komunumo* has initialized but before it starts accepting requests. Currently, plugins must be part of the *Komunumo* source tree. Please add a package below `org.komunumo.plugin` for your plugin source. External JAR-based plugins are not yet supported but may be added in a future version (see issue [#105](https://github.com/McPringle/komunumo/issues/105)).

## Copyright and License

[AGPL License](https://www.gnu.org/licenses/agpl-3.0.de.html)

*Copyright (C) Marcus Fihlon and the individual contributors to **Komunumo**.*

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
