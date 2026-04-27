-- D1 schema for the SikAi content platform.
-- No R2 — files are hosted externally (GitHub Releases, etc.) and
-- referenced by file_url in the manifest.

-- ============================================================
-- Content manifest (past papers, syllabi, textbooks, etc.)
-- ============================================================
CREATE TABLE IF NOT EXISTS content_manifest (
    id              TEXT PRIMARY KEY,
    title           TEXT NOT NULL,
    type            TEXT NOT NULL,  -- past_paper, syllabus, textbook, notes, mcq_pack, model_question, solution
    class_level     INTEGER NOT NULL,
    subject         TEXT NOT NULL,
    year            INTEGER,
    file_url        TEXT,           -- direct download URL (GitHub Release, CDN, etc.)
    size_bytes      INTEGER NOT NULL DEFAULT 0,
    checksum_sha256 TEXT,
    version         INTEGER NOT NULL DEFAULT 1,
    updated_at      INTEGER NOT NULL,
    language        TEXT NOT NULL DEFAULT 'en',
    tags_csv        TEXT NOT NULL DEFAULT ''
);

CREATE INDEX IF NOT EXISTS idx_manifest_class_level ON content_manifest(class_level);
CREATE INDEX IF NOT EXISTS idx_manifest_subject     ON content_manifest(subject);
CREATE INDEX IF NOT EXISTS idx_manifest_type        ON content_manifest(type);

-- ============================================================
-- Quiz questions (MCQs for classes 8, 10, 12)
-- ============================================================
CREATE TABLE IF NOT EXISTS question (
    id              TEXT PRIMARY KEY,
    class_level     INTEGER NOT NULL,
    subject         TEXT NOT NULL,
    topic           TEXT NOT NULL DEFAULT 'general',
    prompt          TEXT NOT NULL,
    options_csv     TEXT NOT NULL,     -- pipe-separated options
    correct_index   INTEGER NOT NULL,
    explanation     TEXT,
    source          TEXT NOT NULL DEFAULT 'seed',  -- seed, ai, admin
    language        TEXT NOT NULL DEFAULT 'en',
    difficulty      TEXT NOT NULL DEFAULT 'medium',  -- easy, medium, hard
    created_at      INTEGER NOT NULL DEFAULT (unixepoch() * 1000)
);

CREATE INDEX IF NOT EXISTS idx_question_class_subject ON question(class_level, subject);
CREATE INDEX IF NOT EXISTS idx_question_class        ON question(class_level);

-- ============================================================
-- Subjects per class level
-- ============================================================
CREATE TABLE IF NOT EXISTS subject (
    id              TEXT PRIMARY KEY,
    display_name    TEXT NOT NULL,
    class_level     INTEGER NOT NULL,
    icon            TEXT,             -- emoji or icon name
    sort_order      INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_subject_class ON subject(class_level);

-- ============================================================
-- Analytics events (quiz completions, downloads, etc.)
-- ============================================================
CREATE TABLE IF NOT EXISTS analytics_event (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    device_id       TEXT NOT NULL,
    event_type      TEXT NOT NULL,     -- quiz_complete, download, app_open, page_view
    class_level     INTEGER,
    subject         TEXT,
    metadata        TEXT,              -- JSON blob for extra data
    created_at      INTEGER NOT NULL DEFAULT (unixepoch() * 1000)
);

CREATE INDEX IF NOT EXISTS idx_analytics_device   ON analytics_event(device_id);
CREATE INDEX IF NOT EXISTS idx_analytics_type     ON analytics_event(event_type);
CREATE INDEX IF NOT EXISTS idx_analytics_created  ON analytics_event(created_at);

-- ============================================================
-- Admin sessions
-- ============================================================
CREATE TABLE IF NOT EXISTS admin_session (
    id              TEXT PRIMARY KEY,
    token_hash      TEXT NOT NULL,
    created_at      INTEGER NOT NULL DEFAULT (unixepoch() * 1000),
    expires_at      INTEGER NOT NULL
);

-- ============================================================
-- App config (feature flags, announcements, etc.)
-- ============================================================
CREATE TABLE IF NOT EXISTS app_config (
    key             TEXT PRIMARY KEY,
    value           TEXT NOT NULL,
    updated_at      INTEGER NOT NULL DEFAULT (unixepoch() * 1000)
);

INSERT OR IGNORE INTO app_config (key, value) VALUES
    ('min_app_version', '1'),
    ('maintenance_mode', 'false'),
    ('announcement_text', ''),
    ('announcement_active', 'false');