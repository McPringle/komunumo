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
'## About

Organization  
Firstname Lastname  
Street and No.  
ZIP City  
Country

E-Mail: [email@example.eu](mailto:email@example.eu)

Platform of the EU Commission for online dispute resolution:  
[https://ec.europa.eu/consumers/odr](https://ec.europa.eu/consumers/odr)

---

For more information about the software used on this website, *Komunumo*, please visit the website: [https://komunumo.app](https://komunumo.app)'),

-- German version
('about', 'DE', NOW(), NOW(), 'Impressum',
'## Impressum

**Angaben gemäß § 5 TMG:**

Organisation  
Vorname Nachname  
Strasse und Nr.  
PLZ Ort  
Land

E-Mail: [email@example.eu](mailto:email@example.eu)

**Verantwortlich für den Inhalt nach § 18 Abs. 2 MStV:**  
Vorname Nachname (Anschrift wie oben)

Keine Umsatzsteuer-Identifikationsnummer vorhanden.  
Nicht eingetragen im Handelsregister.

Plattform der EU-Kommission zur Online-Streitbeilegung:  
[https://ec.europa.eu/consumers/odr](https://ec.europa.eu/consumers/odr)

---

Für mehr Informationen über die auf dieser Webseite verwendete Software, *Komunumo*, besuche bitte die Website: [https://komunumo.app](https://komunumo.app)');

-- [jooq ignore stop]
