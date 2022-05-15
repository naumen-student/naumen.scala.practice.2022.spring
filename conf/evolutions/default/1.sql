-- Jobs schema

-- !Ups

CREATE TABLE jobs (
  id TEXT PRIMARY KEY,
  title TEXT,
  requirement TEXT,
  responsibility TEXT,
  url TEXT
);

-- !Downs

DROP TABLE jobs;