CREATE TYPE positions_enum AS ENUM ('GK', 'DEF', 'MIDF', 'STR');

CREATE TYPE continents_enum AS ENUM ('AFRICA', 'EUROPA', 'ASIA', 'AMERICA');

CREATE TABLE "Team" (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    continent continents_enum NOT NULL
);


CREATE TABLE "Player" (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    "position" positions_enum NOT NULL,
    id_team INT,
    CONSTRAINT fk_team FOREIGN KEY (id_team) REFERENCES "Team"(id) ON DELETE CASCADE
);


-- add the new column for Player table
ALTER TABLE "Player"
ADD COLUMN goal_nb INT;