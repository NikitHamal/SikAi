CREATE TABLE IF NOT EXISTS questions (
  id TEXT PRIMARY KEY,
  classLevel INTEGER NOT NULL,
  subject TEXT NOT NULL,
  topic TEXT NOT NULL,
  prompt TEXT NOT NULL,
  optionsCsv TEXT NOT NULL,
  answerIndex INTEGER NOT NULL,
  explanation TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_questions_lookup ON questions(classLevel, subject, topic);
