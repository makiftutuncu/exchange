CREATE TABLE rates(
    source CHAR(3) NOT NULL,
    target CHAR(3) NOT NULL,
    rate DOUBLE NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY(source, target)
);
