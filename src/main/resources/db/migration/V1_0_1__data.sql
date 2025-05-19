-- Attention: Markdown line breaks in this file rely on two spaces at the end of a line.
-- Please make sure your editor does not remove trailing spaces when saving!
-- IntelliJ: Settings → Editor → General → On Save → set "Remove trailing spaces" to "None" or use .editorconfig.

-- [jooq ignore start]

INSERT INTO mail_template (id, language, subject, markdown)
VALUES ('USER_LOGIN_CONFIRMATION','DE','Deine Anmeldung bei Komunumo', 'Bitte klicke auf den folgenden Link, um dich bei Komunumo anzumelden:\n${login_link}'),
       ('USER_LOGIN_CONFIRMATION','EN','Your Login Request at Komunumo', 'Please click on the following link to log in to Komunumo:\n${login_link}');

INSERT INTO global_page (slot, language, created, updated, title, markdown)
VALUES
-- English version
('about', 'EN', NOW(), NOW(), 'About',
'## About *Komunumo*

Are you a community leader? *Komunumo* makes your life a lot easier! Everything you need to run your community in one place – for free! *Komunumo* is an open source community management software that helps you with all tasks of managing a community, such as a user group.

With *Komunumo*, you can manage your community members (including self-management), organize events (including registration), send newsletters, manage your sponsors, and much more.

We aim to implement all key features of [Meetup](https://www.meetup.com/) in *Komunumo*. Our goal is full compatibility with the [Fediverse](https://en.wikipedia.org/wiki/Fediverse), enabling seamless data exchange with [Mastodon](https://en.wikipedia.org/wiki/Mastodon_(social_network)), [Mobilizon](https://en.wikipedia.org/wiki/Mobilizon), and other services that support the [ActivityPub](https://en.wikipedia.org/wiki/ActivityPub) protocol.

*Komunumo* is free software licensed under the [GNU Affero General Public License (AGPL)](https://www.gnu.org/licenses/agpl-3.0.html). It is developed by a passionate [group of people](https://github.com/McPringle/komunumo/blob/main/CONTRIBUTORS.md) who actively contribute to various communities in their free time. No one earns money with *Komunumo*. *Komunumo* belongs to the community!

For development we use a [GitHub repository](https://github.com/McPringle/komunumo), where we [track issues](https://github.com/McPringle/komunumo/issues) and host a [forum for discussions and support](https://github.com/McPringle/komunumo/discussions).

We use the [Java](https://en.wikipedia.org/wiki/Java_(programming_language)) programming language and the [Vaadin](https://vaadin.com/) framework for the server. *Komunumo* is still under heavy development – and **any kind of help is highly appreciated!**'),

-- German version
('about', 'DE', NOW(), NOW(), 'Über',
'## Über *Komunumo*

Du leitest eine Community? *Komunumo* macht dir das Leben leichter! Alles, was du für deine Community brauchst – an einem Ort, kostenlos! *Komunumo* ist eine quelloffene Software zur Community-Verwaltung, die dich bei allen Aufgaben unterstützt – etwa bei der Organisation einer User Group.

Mit *Komunumo* kannst du Mitglieder verwalten (inklusive Selbstverwaltung), Veranstaltungen organisieren (mit Anmeldung), Newsletter verschicken, Sponsoren betreuen und vieles mehr.

Unser Ziel ist es, alle wichtigen Funktionen von [Meetup](https://www.meetup.com/) in *Komunumo* bereitzustellen. Dabei setzen wir auf vollständige Kompatibilität mit dem [Fediverse](https://de.wikipedia.org/wiki/Fediverse), um einen reibungslosen Datenaustausch mit [Mastodon](https://de.wikipedia.org/wiki/Mastodon_(soziales_Netzwerk)), [Mobilizon](https://mobilizon.org/de/) und anderen Diensten zu ermöglichen, die das [ActivityPub](https://de.wikipedia.org/wiki/ActivityPub)-Protokoll unterstützen.

*Komunumo* ist freie Software unter der [GNU Affero General Public License (AGPL)](https://www.gnu.org/licenses/agpl-3.0.html) und kann von allen genutzt werden. Entwickelt wird *Komunumo* von einer engagierten [Gruppe von Menschen](https://github.com/McPringle/komunumo/blob/main/CONTRIBUTORS.md), die in verschiedenen Communities aktiv sind und ihre Freizeit in das Projekt investieren. Niemand verdient Geld mit *Komunumo* – *Komunumo* gehört der Community!

Die Entwicklung erfolgt über unser [GitHub-Repository](https://github.com/McPringle/komunumo), wo wir [Issues verwalten](https://github.com/McPringle/komunumo/issues) und ein [Forum für Diskussionen und Support](https://github.com/McPringle/komunumo/discussions) betreiben.

Wir verwenden die Programmiersprache [Java](https://de.wikipedia.org/wiki/Java_(Programmiersprache)) und das Framework [Vaadin](https://vaadin.com/) für den Server. *Komunumo* befindet sich noch in aktiver Entwicklung – **jede Hilfe ist herzlich willkommen!**');

-- [jooq ignore stop]
