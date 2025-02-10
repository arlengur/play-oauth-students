# --- !Ups

CREATE TABLE students
(
    id          integer AUTO_INCREMENT PRIMARY KEY,
    first_name  varchar(50) NOT NULL,
    middle_name varchar(50) NOT NULL,
    last_name   varchar(50) NOT NULL,
    group_id    integer     NOT NULL,
    avg_score double NOT NULL
);

# --- !Downs
DROP TABLE students IF EXISTS;