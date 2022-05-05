-- Create Table

-- !Ups
CREATE TABLE jobtable (
    id VARCHAR(32),
    title VARCHAR(512),
    requirements VARCHAR(512),
    responsibility VARCHAR(512),
    salaryFrom INT,
    salaryTo INT,
    salaryCurr VARCHAR(8),
    url VARCHAR(512),
    PRIMARY KEY (id)
);

-- !Downs
DROP TABLE jobtable;