--
-- Copyright (c) 2016, Crossover and/or its affiliates. All rights reserved.
-- CROSSOVER PROPRIETARY/CONFIDENTIAL.
--
--     https://www.crossover.com/
--

START TRANSACTION;

DROP DATABASE IF EXISTS Crossover;
CREATE DATABASE IF NOT EXISTS Crossover
    DEFAULT CHARACTER SET utf8
    DEFAULT COLLATE utf8_general_ci;
USE Crossover;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS Crossover.application cascade;
DROP TABLE IF EXISTS Crossover.log cascade;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE Crossover.application
(
    application_id varchar(32) NOT NULL,
    display_name   varchar(32) NOT NULL,
    secret         varchar(32) NOT NULL,
    PRIMARY KEY (application_id)
);

CREATE TABLE Crossover.log
(
    log_id         bigint           NOT NULL AUTO_INCREMENT,
    application_id varchar(32)   NOT NULL,
    logger         varchar(256)  NOT NULL,
    level          varchar(256)  NOT NULL,
    message        varchar(2048) NOT NULL,
    PRIMARY KEY (log_id),
    FOREIGN KEY (application_id) REFERENCES application (application_id)
);


CREATE TABLE Crossover.application_properties
(
    property_name  varchar(64) NOT NULL,
    property_value varchar(64)   NOT NULL,
    PRIMARY KEY (property_name)
);

CREATE TABLE Crossover.authentication
(
    id              bigint          NOT NULL AUTO_INCREMENT,
    application_id  varchar(32) NOT NULL,
    access_token varchar(32)   NOT NULL,
    authentication_time  TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (application_id) REFERENCES application (application_id)
);

CREATE INDEX auth_index ON Crossover.authentication (application_id, authentication_time);

CREATE USER 'crossover'@'localhost' IDENTIFIED BY 'crossover';
GRANT ALL PRIVILEGES ON Crossover.* TO 'crossover'@'localhost';


COMMIT;