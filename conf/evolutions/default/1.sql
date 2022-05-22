-- Create Table

-- !Ups
CREATE TABLE jobTable (
    id VARCHAR(32),
    title VARCHAR(512),
    requirements TEXT NULL,
    responsibility TEXT NULL,
    salaryFrom INT,
    salaryTo INT,
    salaryCurr VARCHAR(8) NULL,
    url TEXT,
    PRIMARY KEY (id)
);

CREATE TABLE jobRequestTable (
    jobId VARCHAR(32),
    city VARCHAR(64) NULL,
    keyword TEXT NULL,
    FOREIGN KEY(jobid) REFERENCES jobtable(id)
)

-- !Downs
DROP TABLE jobRequestTable CASCADE;

DROP TABLE jobtable;