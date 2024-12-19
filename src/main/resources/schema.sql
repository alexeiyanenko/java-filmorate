DROP TABLE IF EXISTS likes CASCADE;
DROP TABLE IF EXISTS friends CASCADE;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS film_genre CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS mpa CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS events CASCADE;

SET SCHEMA PUBLIC;

CREATE TABLE users (
                       user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       user_name varchar(50),
                       email varchar(50) NOT NULL,
                       login varchar(50) NOT NULL,
                       birthday date NOT NULL
);

CREATE TABLE mpa (
                     mpa_id IDENTITY PRIMARY KEY,
                     mpa_name varchar(15) NOT NULL,
                     description varchar(100) NOT NULL
);

CREATE TABLE genres (
                        genre_id IDENTITY PRIMARY KEY,
                        genre_name varchar(100)
);

CREATE TABLE films (
                       film_id IDENTITY PRIMARY KEY,
                       film_name varchar(100) NOT NULL,
                       description varchar(200),
                       release_date date,
                       duration integer,
                       mpa_id INTEGER REFERENCES mpa(mpa_id) ON DELETE CASCADE
);

CREATE TABLE friends (
                         sender_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
                         receiver_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
                         status VARCHAR(50) NOT NULL,
                         PRIMARY KEY (sender_id, receiver_id)
);

CREATE TABLE film_genre (
                            film_id INTEGER REFERENCES films(film_id) ON DELETE CASCADE,
                            genre_id INTEGER REFERENCES genres(genre_id) ON DELETE CASCADE,
                            PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE likes (
                       user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
                       film_id INTEGER REFERENCES films(film_id) ON DELETE CASCADE,
                       PRIMARY KEY (user_id, film_id)
);

CREATE TABLE IF NOT EXISTS events (
                                      event_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                                      timestamp BIGINT NOT NULL,
                                      event_type VARCHAR(50) NOT NULL,
                                      operation VARCHAR(50) NOT NULL,
                                      entity_id BIGINT NOT NULL
);


