-- SikAi D1 schema. Apply with:
--   wrangler d1 execute sikai --file=backend/schema.sql --remote

CREATE TABLE IF NOT EXISTS manifest (
  id              TEXT PRIMARY KEY,
  title           TEXT NOT NULL,
  type            TEXT NOT NULL,            -- textbook | past_paper | mcq_pack | syllabus | notes
  classLevel      INTEGER NOT NULL,
  subject         TEXT NOT NULL,
  year            INTEGER,
  fileKey         TEXT NOT NULL,            -- R2 object key
  sizeBytes       INTEGER NOT NULL,
  checksumSha256  TEXT NOT NULL,
  version         INTEGER NOT NULL DEFAULT 1,
  updatedAt       INTEGER NOT NULL,
  language        TEXT NOT NULL DEFAULT 'en',
  tagsCsv         TEXT NOT NULL DEFAULT '',
  description     TEXT NOT NULL DEFAULT ''
);

CREATE INDEX IF NOT EXISTS idx_manifest_class   ON manifest(classLevel);
CREATE INDEX IF NOT EXISTS idx_manifest_subject ON manifest(subject);
CREATE INDEX IF NOT EXISTS idx_manifest_type    ON manifest(type);

CREATE TABLE IF NOT EXISTS questions (
  id           TEXT PRIMARY KEY,
  classLevel   INTEGER NOT NULL,
  subject      TEXT NOT NULL,
  topic        TEXT NOT NULL,
  prompt       TEXT NOT NULL,
  optionsJson  TEXT NOT NULL,                -- JSON array of strings
  correctIndex INTEGER NOT NULL,
  explanation  TEXT NOT NULL DEFAULT '',
  difficulty   INTEGER NOT NULL DEFAULT 2,
  source       TEXT NOT NULL DEFAULT 'curated',
  createdAt    INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_q_class   ON questions(classLevel);
CREATE INDEX IF NOT EXISTS idx_q_subject ON questions(subject);
CREATE INDEX IF NOT EXISTS idx_q_topic   ON questions(topic);
