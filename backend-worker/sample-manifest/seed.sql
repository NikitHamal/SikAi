-- Seed subjects for NEB/SEE classes 8, 10, 12
INSERT OR IGNORE INTO subject (id, display_name, class_level, icon, sort_order) VALUES
    ('8-math',       'Mathematics',      8, '📐', 1),
    ('8-science',    'Science',           8, '🔬', 2),
    ('8-english',    'English',          8, '📖', 3),
    ('8-nepali',     'Nepali',            8, '📝', 4),
    ('8-social',     'Social Studies',    8, '🌍', 5),
    ('8-health',     'Health & PE',       8, '💪', 6),
    ('8-computer',   'Computer',          8, '💻', 7),
    ('10-math',      'Mathematics',      10, '📐', 1),
    ('10-science',   'Science',          10, '🔬', 2),
    ('10-english',   'English',          10, '📖', 3),
    ('10-nepali',    'Nepali',           10, '📝', 4),
    ('10-social',    'Social Studies',   10, '🌍', 5),
    ('10-health',    'Health & PE',      10, '💪', 6),
    ('10-computer',  'Computer',         10, '💻', 7),
    ('12-physics',   'Physics',          12, '⚛️', 1),
    ('12-chemistry', 'Chemistry',        12, '🧪', 2),
    ('12-biology',   'Biology',          12, '🧬', 3),
    ('12-math',      'Mathematics',      12, '📐', 4),
    ('12-english',   'English',          12, '📖', 5),
    ('12-nepali',    'Nepali',           12, '📝', 6),
    ('12-computer',  'Computer Science', 12, '💻', 7);

-- Seed content manifest entries.
-- file_url points to GitHub Releases or any direct download URL.
-- Replace <OWNER>/<REPO> with your actual GitHub repo when you upload PDFs.
INSERT OR IGNORE INTO content_manifest
(id, title, type, class_level, subject, year, file_url, size_bytes, checksum_sha256, version, updated_at, language, tags_csv) VALUES
    ('see-2080-math',      'SEE 2080 Mathematics — Full Paper',         'past_paper', 10, 'Mathematics', 2080, 'https://github.com/<OWNER>/<REPO>/releases/download/content/see-2080-math.pdf',      0, NULL, 1, 1740614400000, 'en', 'see,2080,mathematics'),
    ('see-2080-science',   'SEE 2080 Science — Full Paper',             'past_paper', 10, 'Science',     2080, 'https://github.com/<OWNER>/<REPO>/releases/download/content/see-2080-science.pdf',   0, NULL, 1, 1740614400000, 'en', 'see,2080,science'),
    ('see-2080-english',   'SEE 2080 English — Full Paper',             'past_paper', 10, 'English',     2080, 'https://github.com/<OWNER>/<REPO>/releases/download/content/see-2080-english.pdf',   0, NULL, 1, 1740614400000, 'en', 'see,2080,english'),
    ('see-2079-math',      'SEE 2079 Mathematics — Full Paper',         'past_paper', 10, 'Mathematics', 2079, 'https://github.com/<OWNER>/<REPO>/releases/download/content/see-2079-math.pdf',      0, NULL, 1, 1740614400000, 'en', 'see,2079,mathematics'),
    ('see-2079-science',   'SEE 2079 Science — Full Paper',             'past_paper', 10, 'Science',     2079, 'https://github.com/<OWNER>/<REPO>/releases/download/content/see-2079-science.pdf',   0, NULL, 1, 1740614400000, 'en', 'see,2079,science'),
    ('neb-2080-physics',   'NEB Class 12 2080 Physics — Full Paper',   'past_paper', 12, 'Physics',     2080, 'https://github.com/<OWNER>/<REPO>/releases/download/content/neb-2080-physics.pdf',   0, NULL, 1, 1740614400000, 'en', 'neb,2080,physics'),
    ('neb-2080-chemistry', 'NEB Class 12 2080 Chemistry — Full Paper', 'past_paper', 12, 'Chemistry',   2080, 'https://github.com/<OWNER>/<REPO>/releases/download/content/neb-2080-chemistry.pdf', 0, NULL, 1, 1740614400000, 'en', 'neb,2080,chemistry'),
    ('neb-2080-math',      'NEB Class 12 2080 Mathematics — Full Paper','past_paper', 12, 'Mathematics', 2080, 'https://github.com/<OWNER>/<REPO>/releases/download/content/neb-2080-math.pdf',      0, NULL, 1, 1740614400000, 'en', 'neb,2080,mathematics'),
    ('class8-math-syl',    'Class 8 Mathematics Syllabus (CDC)',        'syllabus',    8, 'Mathematics', NULL, 'https://github.com/<OWNER>/<REPO>/releases/download/content/class8-math-syl.pdf',    0, NULL, 1, 1740614400000, 'en', 'cdc,syllabus,class8'),
    ('class10-syl-pack',   'Class 10 SEE Syllabus Pack (CDC)',          'syllabus',   10, 'All',         NULL, 'https://github.com/<OWNER>/<REPO>/releases/download/content/class10-syl-pack.pdf',   0, NULL, 1, 1740614400000, 'en', 'cdc,syllabus,see'),
    ('class12-syl-pack',   'Class 12 NEB Syllabus Pack',               'syllabus',   12, 'All',         NULL, 'https://github.com/<OWNER>/<REPO>/releases/download/content/class12-syl-pack.pdf',   0, NULL, 1, 1740614400000, 'en', 'cdc,syllabus,neb');

-- Seed MCQ questions across classes 8, 10, 12
INSERT OR IGNORE INTO question (id, class_level, subject, topic, prompt, options_csv, correct_index, explanation, source, language, difficulty) VALUES
    ('q8-math-001', 8, 'Mathematics', 'Algebra', 'If x + 5 = 12, what is the value of x?', '5|6|7|8', 2, 'Subtract 5 from both sides: x = 12 - 5 = 7', 'seed', 'en', 'easy'),
    ('q8-math-002', 8, 'Mathematics', 'Geometry', 'What is the area of a triangle with base 10 cm and height 6 cm?', '60 sq cm|30 sq cm|16 sq cm|20 sq cm', 1, 'Area = ½ × base × height = ½ × 10 × 6 = 30 sq cm', 'seed', 'en', 'easy'),
    ('q8-math-003', 8, 'Mathematics', 'Fractions', 'What is 3/4 + 1/4?', '4/4 or 1|2/4|4/8|1/2', 0, '3/4 + 1/4 = 4/4 = 1', 'seed', 'en', 'easy'),
    ('q8-sci-001', 8, 'Science', 'Physics', 'What is the SI unit of force?', 'Joule|Newton|Pascal|Watt', 1, 'The SI unit of force is Newton (N), named after Sir Isaac Newton.', 'seed', 'en', 'easy'),
    ('q8-sci-002', 8, 'Science', 'Chemistry', 'Which of the following is an example of a chemical change?', 'Melting ice|Boiling water|Rusting of iron|Breaking glass', 2, 'Rusting is a chemical change because a new substance (iron oxide) is formed.', 'seed', 'en', 'easy'),
    ('q10-math-001', 10, 'Mathematics', 'Quadratic Equations', 'If the discriminant of a quadratic equation is zero, the roots are:', 'Real and equal|Real and unequal|Imaginary|Not defined', 0, 'When D = 0, the quadratic has two equal real roots.', 'seed', 'en', 'medium'),
    ('q10-math-002', 10, 'Mathematics', 'Trigonometry', 'What is the value of sin 30°?', '0|1/2|√3/2|1', 1, 'sin 30° = 1/2', 'seed', 'en', 'easy'),
    ('q10-math-003', 10, 'Mathematics', 'Statistics', 'The mean of 2, 4, 6, 8, 10 is:', '5|6|7|30', 1, 'Mean = (2+4+6+8+10)/5 = 30/5 = 6', 'seed', 'en', 'easy'),
    ('q10-math-004', 10, 'Mathematics', 'Probability', 'A die is thrown once. What is the probability of getting a number greater than 4?', '1/6|1/3|2/3|1/2', 1, 'Numbers > 4 are 5 and 6. P = 2/6 = 1/3', 'seed', 'en', 'medium'),
    ('q10-sci-001', 10, 'Science', 'Physics', 'The SI unit of electric current is:', 'Volt|Ohm|Ampere|Watt', 2, 'The SI unit of electric current is Ampere (A).', 'seed', 'en', 'easy'),
    ('q10-sci-002', 10, 'Science', 'Chemistry', 'Which gas is evolved when zinc reacts with dilute hydrochloric acid?', 'Oxygen|Hydrogen|Chlorine|Nitrogen', 1, 'Zn + 2HCl → ZnCl₂ + H₂. Hydrogen gas is evolved.', 'seed', 'en', 'medium'),
    ('q10-sci-003', 10, 'Science', 'Biology', 'Photosynthesis takes place in which part of the plant cell?', 'Mitochondria|Nucleus|Chloroplast|Ribosome', 2, 'Photosynthesis occurs in chloroplasts which contain chlorophyll.', 'seed', 'en', 'easy'),
    ('q10-eng-001', 10, 'English', 'Grammar', 'Choose the correct passive voice: "She writes a letter."', 'A letter is written by her.|A letter was written by her.|A letter has been written by her.|A letter is being written by her.', 0, 'Present simple passive: is + past participle.', 'seed', 'en', 'medium'),
    ('q12-phy-001', 12, 'Physics', 'Electrostatics', 'The electric field inside a charged conductor is:', 'Zero|Maximum|Minimum|Infinite', 0, 'By Gauss''s law, the electric field inside a charged conductor is zero.', 'seed', 'en', 'medium'),
    ('q12-phy-002', 12, 'Physics', 'Optics', 'The focal length of a concave mirror is 15 cm. Its radius of curvature is:', '7.5 cm|15 cm|30 cm|45 cm', 2, 'R = 2f = 2 × 15 = 30 cm', 'seed', 'en', 'easy'),
    ('q12-chem-001', 12, 'Chemistry', 'Organic Chemistry', 'The IUPAC name of CH₃CHO is:', 'Methanal|Ethanal|Propanal|Acetone', 1, 'CH₃CHO has 2 carbons, so it is Ethanal (Acetaldehyde).', 'seed', 'en', 'medium'),
    ('q12-bio-001', 12, 'Biology', 'Genetics', 'Which blood group is known as the universal donor?', 'A|B|AB|O', 3, 'Blood group O negative is the universal donor as it has no antigens.', 'seed', 'en', 'easy');