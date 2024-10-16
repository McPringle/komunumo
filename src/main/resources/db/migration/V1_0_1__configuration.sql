-- [jooq ignore start]

INSERT INTO `configuration` (`conf_key`, `conf_value`)
VALUES  ('website.url', 'http://localhost:8080'),
        ('website.favicon', 'https://static.jug.ch/images/favicon.ico'),
        ('website.name', 'Java User Group Switzerland'),
        ('website.contact.address', '8000 Zürich'),
        ('website.contact.email', 'info@jug.ch'),
        ('website.copyright', '© Java User Group Switzerland'),
        ('website.about.text', '<p style="max-width: 320px;">JUG Switzerland aims at promoting the application of Java technology in Switzerland.</p><p style="max-width: 320px;">JUG Switzerland facilitates the sharing of experience and information among its members. This is accomplished through workshops, seminars and conferences. JUG Switzerland supports and encourages the cooperation between commercial organizations and research institutions.</p><p style="max-width: 320px;">JUG Switzerland is funded through membership fees and industry sponsors.</p>'),
        ('website.logo.template', 'https://static.jug.ch/images/logos/jugs_logo_%02d.gif'),
        ('website.logo.min', '1'),
        ('website.logo.max', '22');

-- [jooq ignore stop]
