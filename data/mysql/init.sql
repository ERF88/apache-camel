CREATE DATABASE camel;

USE camel;

CREATE TABLE negotiation(
    id BIGINT NOT NULL auto_increment PRIMARY KEY,
    price DECIMAL(5,2),
    quantity INT,
    `date` DATE
) engine=innodb;