# Komunumo

[![All Tests](https://github.com/McPringle/komunumo/actions/workflows/all-tests.yml/badge.svg)](https://github.com/McPringle/komunumo/actions/workflows/all-tests.yml)
[![codecov](https://codecov.io/github/McPringle/komunumo/graph/badge.svg?token=6MTYWYK083)](https://codecov.io/github/McPringle/komunumo)

**Open Source Community Manager**

*Komunumo* is an esperanto noun with a meaning of *community*.

## About

You are are community leader? Komunumo makes your life a lot easier! Eveything you need to run your community in one place, for free! Komunumo is an open source community management software which assists you in all tasks managing a community like a user group.

With Komunumo you can manage your community members (including self-management), organize events (including registration), send out newsletters, manage your sponsors, and a lot more.

We have set ourselves the goal of implementing all important [Meetup](https://www.meetup.com/) functions for communities in Komunumo. We strive for full compatibility with the [Fediverse](https://en.wikipedia.org/wiki/Fediverse) to ensure smooth data exchange with [Mastodon](https://en.wikipedia.org/wiki/Mastodon_(social_network)), [Mobilizon](https://en.wikipedia.org/wiki/Mobilizon), and other services using the [ActivityPub](https://en.wikipedia.org/wiki/ActivityPub) protocol.

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

The server runs on port 8080 by default. If you don't like it, change it:

```
PORT=8080
```

### Database

*Komunumo* needs a database to store the business data. By default, *Komunumo* comes with [MariaDB](https://mariadb.org/) drivers. MariaDB is recommended because we are using it during development, and it is highly tested with *Komunumo*. All free and open source JDBC compatible databases are supported, but you need to configure the JDBC driver dependencies accordingly. Please make sure that your database is using a Unicode character set to avoid problems storing data containing Unicode characters.

The `DB_USER` is used to access the *Komunumo* database including automatic schema migrations and needs `ALL PRIVILEGES`.

```
DB_URL=jdbc:mariadb://localhost:3306/komunumo?serverTimezone\=Europe/Zurich&allowMultiQueries=true
DB_USER=johndoe
DB_PASS=verysecret
```

The database schema will be migrated automatically by *Komunumo*.

#### Important MySQL and MariaDB configuration

MySQL and MariaDB have a possible silent truncation problem with the `GROUP_CONCAT` command. To avoid this it is necessary, to configure these two databases to allow multi queries. Just add `allowMultiQueries=true` to the JDBC database URL like in this example (you may need to scroll the example code to the right):

```
DB_URL=jdbc:mariadb://localhost:3306/komunumo?serverTimezone\=Europe/Zurich&allowMultiQueries=true
```

## Copyright and License

[AGPL License](https://www.gnu.org/licenses/agpl-3.0.de.html)

*Copyright (C) Marcus Fihlon and the individual contributors to **Komunumo**.*

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
