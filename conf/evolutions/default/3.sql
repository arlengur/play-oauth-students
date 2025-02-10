# --- !Ups

CREATE TABLE oauth_app
(
    id           integer AUTO_INCREMENT PRIMARY KEY,
    client_id    varchar(50) NOT NULL,
    access_token varchar(50) NULL,
    user_id      integer NULL
);

# --- !Downs
DROP TABLE oauth_app IF EXISTS;