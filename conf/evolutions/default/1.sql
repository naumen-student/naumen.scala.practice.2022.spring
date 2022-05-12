-- Create Table

-- !Ups
CREATE TABLE jobtable (
    id VARCHAR(32),
    title VARCHAR(512),
    requirements TEXT,
    responsibility TEXT,
    salaryFrom INT,
    salaryTo INT,
    salaryCurr VARCHAR(8),
    url TEXT,
    PRIMARY KEY (id)
);

CREATE TABLE jobrequesttable (
    jobid VARCHAR(32),
    city VARCHAR(64),
    keyword TEXT,
    FOREIGN KEY(jobid) REFERENCES jobtable(id)
)

-- !Downs
DROP TABLE jobrequesttable CASCADE;

DROP TABLE jobtable;