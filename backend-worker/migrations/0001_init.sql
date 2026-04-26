-- D1 schema for the SikAi content manifest. The Android client mirrors this
-- shape into Room (`content_manifest` table) so column names map 1:1 with the
-- TypeScript projection in src/index.ts.

CREATE TABLE IF NOT EXISTS content_manifest (
    id              TEXT PRIMARY KEY,
    title           TEXT NOT NULL,
    -- one of: "past_paper", "syllabus", "notes", "model_question", "solution"
    type            TEXT NOT NULL,
    class_level     INTEGER NOT NULL,
    subject         TEXT NOT NULL,
    year            INTEGER,
    -- direct download URL (CDN, GitHub raw, etc.) OR null when serving via R2
    file_url        TEXT,
    -- R2 object key when the file is stored in this worker's bucket
    file_key        TEXT,
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
