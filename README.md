# Komunumo

[![All Tests](https://github.com/McPringle/komunumo/actions/workflows/all-tests.yml/badge.svg)](https://github.com/McPringle/komunumo/actions/workflows/all-tests.yml)
[![codecov](https://codecov.io/github/McPringle/komunumo/graph/badge.svg?token=6MTYWYK083)](https://codecov.io/github/McPringle/komunumo)

**Open Source Community Manager**

*Komunumo* is an esperanto noun with a meaning of *community*.

## About

You are a community leader? *Komunumo* makes your life a lot easier! Everything you need to run your community in one place, for free! *Komunumo* is an open source community manager software which assists you in all tasks managing a community like a user group or a meetup.

With *Komunumo* you can manage your community members (including self-management), organize events (including registration), send out newsletters, manage your sponsors, and a lot more.

We have set ourselves the goal of implementing all important [Meetup](https://www.meetup.com/) functions for communities in *Komunumo*. We strive for full compatibility with the [Fediverse](https://en.wikipedia.org/wiki/Fediverse) to ensure smooth data exchange with [Mastodon](https://en.wikipedia.org/wiki/Mastodon_(social_network)), [Mobilizon](https://en.wikipedia.org/wiki/Mobilizon), and other services using the [ActivityPub](https://en.wikipedia.org/wiki/ActivityPub) protocol.

## Versioning

*Komunumo* follows [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html). Version numbers are structured as `MAJOR.MINOR.PATCH`:

- **MAJOR** versions introduce incompatible API or data changes,
- **MINOR** versions add functionality in a backwards-compatible manner,
- **PATCH** versions include backwards-compatible bug fixes.

This versioning scheme applies to the public API, database schema, and plugin interfaces. We aim to keep upgrades predictable and manageable. Breaking changes, new features, and fixes are documented in the [CHANGELOG.md](CHANGELOG.md) and in the [release notes](https://github.com/McPringle/komunumo/releases).

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

### Server Configuration

The server runs on port 8080 by default. If you don't like it, change it using an environment variable:

```
KOMUNUMO_PORT=8080
```

### Instance Configuration

#### Admin

When starting *Komunumo* for the first time, the system can automatically create an initial instance admin. To enable this, set the following environment variable to the email address of the admin:

```
KOMUNUMO_INSTANCE_ADMIN=admin@example.eu
```

If no user with the admin role is found in the database, a new instance admin will be created with the email address given.

> [!WARNING]
> This mechanism only runs once! It will **not** overwrite or recreate users if an admin already exists.

#### Name and Slogan

The default name of the instance is *Komunumo* and the default slogan is *Open Source Community Manager*. You can change this to a more suitable name for your instance by setting the following environment variables:

```
KOMUNUMO_INSTANCE_NAME=My Community
KOMUNUMO_INSTANCE_SLOGAN=My Slogan
```

### File Configuration

| Variable                 | Default                       | Description                            |
|--------------------------|-------------------------------|----------------------------------------|
| `KOMUNUMO_FILES_BASEDIR` | `${user.home}/.komunumo/data` | Base directory for local file storage. |

*Komunumo* stores files on the local file system, for example uploaded images. The base directory must be writable by the application and should have sufficient disk space. To ensure data integrity, administrators are strongly advised to include this directory in regular backup routines.

The placeholder `${user.home}` refers to the home directory of the system user running the application.

### Demo Mode Configuration

*Komunumo* can be started in demo mode, which will automatically populate the database with sample users, events, and other data. This allows you to explore the application without needing to create content manually. This is useful for testing and demonstration purposes. To enable demo mode, set the following environment variable:

```
KOMUNUMO_DEMO_ENABLED=true
```

> [!WARNING]
> When demo mode is enabled, any data entered will be deleted at each start of the server and at the top of every hour! This ensures a consistent state for repeated testing and demonstrations.

#### Providing Custom Demo Data

If you prefer not to use the default demo data, you can provide your own JSON file containing demo events. The file must be publicly accessible over HTTPS and contain valid JSON in the expected format. To use your own demo data, set the following environment variable:

```
KOMUNUMO_DEMO_JSON=https://example.com/events.json
```

The structure of the JSON file must follow the expected format used by *Komunumo* for demo data. The file should contain an array of community, event, and image objects. Here is an example of the expected JSON structure:

```json
{
    "communities": [
        {
            "communityId": "9a73690b-6dbd-456a-88e9-dc3f77b69aa0",
            "profile": "@demo@example.com",
            "name": "Demo Community",
            "description": "A description of the demo community.",
            "imageId": "0278ec5a-9fe1-4882-85f9-845ca72c2795"
        }
    ],
    "events": [
        {
            "eventId": "07fe5b04-0ea1-43b7-a63a-f4d8b8c29ed6",
            "communityId": "9a73690b-6dbd-456a-88e9-dc3f77b69aa0",
            "title": "Demo Event",
            "description": "A description of the demo event.",
            "location": "Somewhere",
            "begin": "2025-08-06T18:00:00+02:00[Europe/Zurich]",
            "end": "2025-08-06T20:00:00+02:00[Europe/Zurich]",
            "imageId": "4ca05a55-de1e-4571-a833-c9e5e4f4bfba",
            "visibility": "PUBLIC",
            "status": "PUBLISHED"
        }
    ],
    "images": [
        {
            "imageId": "0278ec5a-9fe1-4882-85f9-845ca72c2795",
            "contentType": "image/jpeg",
            "url": "https://example.com/images/demo-community.jpg"
        },
        {
            "imageId": "4ca05a55-de1e-4571-a833-c9e5e4f4bfba",
            "contentType": "image/jpeg",
            "url": "https://example.com/images/demo-event.jpg"
        }
    ]
}
```

- The `communityId`, `eventId`, and `imageId` fields must be valid UUIDs.
- The `begin` and `end` fields in the `events` object must be in ISO 8601 format with a timezone offset, such as `2025-08-06T18:00:00+02:00[Europe/Zurich]`. This ensures correct interpretation of event times, including proper handling of daylight saving time.
- The `imageId` fields in the `communities` and `events` objects must refer to the IDs of the images in the `images` array. The `url` field in each image object must point to a publicly accessible image file. If the `imageId` is blank, the community or event will not have an image associated with it.
- The `visibility` field in the `events` object can be set to `PUBLIC` or `PRIVATE`, to define the visibility of the event.
- The `status` field can be set to `DRAFT`, `PUBLISHED`, or `CANCELED` to reflect the current state of the event.

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

### Database Configuration

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

### Custom Styles Configuration

The visual style of *Komunumo* can be customized by the instance maintainer using custom CSS variables.  
For details on how to configure and apply custom styles, see the [Style Guide](STYLEGUIDE.md).

## Copyright and License

[AGPL License](https://www.gnu.org/licenses/agpl-3.0.de.html)

*Copyright (C) Marcus Fihlon and the individual contributors to **Komunumo**.*

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
