-- Create Table

-- !Ups
CREATE TABLE jobtable (
    db_id uuid,
    id VARCHAR(32),
    title VARCHAR(512),
    requirements VARCHAR(512),
    responsibility VARCHAR(512),
    salary VARCHAR(512),
    url VARCHAR(512),
    PRIMARY KEY (db_id)
);

-- !Downs
DROP TABLE jobtable;