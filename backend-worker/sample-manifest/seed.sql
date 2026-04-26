-- Seed a small set of representative entries so the app shows real-looking
-- past papers and syllabi out-of-the-box. Replace `file_url` values with your
-- own hosting (R2, GitHub releases, etc.) before deploying.

INSERT OR REPLACE INTO content_manifest
(id, title, type, class_level, subject, year, file_url, file_key, size_bytes, checksum_sha256, version, updated_at, language, tags_csv) VALUES
('see-2080-math',     'SEE 2080 Mathematics — full paper',        'past_paper', 10, 'Mathematics', 2080, NULL, 'see/2080/math.pdf',     0, NULL, 1, 1740614400000, 'en', 'see,2080,mathematics'),
('see-2080-science',  'SEE 2080 Science — full paper',            'past_paper', 10, 'Science',     2080, NULL, 'see/2080/science.pdf',  0, NULL, 1, 1740614400000, 'en', 'see,2080,science'),
('see-2080-english',  'SEE 2080 English — full paper',            'past_paper', 10, 'English',     2080, NULL, 'see/2080/english.pdf',  0, NULL, 1, 1740614400000, 'en', 'see,2080,english'),
('see-2079-math',     'SEE 2079 Mathematics — full paper',        'past_paper', 10, 'Mathematics', 2079, NULL, 'see/2079/math.pdf',     0, NULL, 1, 1740614400000, 'en', 'see,2079,mathematics'),
('neb-2080-physics',  'NEB Class 12 2080 Physics — full paper',   'past_paper', 12, 'Physics',     2080, NULL, 'neb/2080/physics.pdf',  0, NULL, 1, 1740614400000, 'en', 'neb,2080,physics'),
('neb-2080-chem',     'NEB Class 12 2080 Chemistry — full paper', 'past_paper', 12, 'Chemistry',   2080, NULL, 'neb/2080/chem.pdf',     0, NULL, 1, 1740614400000, 'en', 'neb,2080,chemistry'),
('class8-math-syl',   'Class 8 Mathematics Syllabus (CDC)',       'syllabus',    8, 'Mathematics', NULL, NULL, 'cdc/class8/math.pdf',   0, NULL, 1, 1740614400000, 'en', 'cdc,syllabus,class8'),
('class10-syl-pack',  'Class 10 SEE Syllabus Pack (CDC)',         'syllabus',   10, 'All',         NULL, NULL, 'cdc/class10/pack.pdf',  0, NULL, 1, 1740614400000, 'en', 'cdc,syllabus,see'),
('class12-syl-pack',  'Class 12 NEB Syllabus Pack',               'syllabus',   12, 'All',         NULL, NULL, 'cdc/class12/pack.pdf',  0, NULL, 1, 1740614400000, 'en', 'cdc,syllabus,neb');
