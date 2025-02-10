# --- !Ups

CREATE TABLE users
(
    id       integer AUTO_INCREMENT PRIMARY KEY,
    login    varchar(50) NOT NULL,
    password varchar(50) NOT NULL
);

# --- !Downs
DROP TABLE users IF EXISTS;