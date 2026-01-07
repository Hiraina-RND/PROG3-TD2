INSERT INTO "Team" (id, name, continent)
VALUES
(1, 'Real Madrid CF', 'EUROPA'),
(2, 'FC Barcelona', 'EUROPA'),
(3, 'Atlético de Madrid', 'EUROPA'),
(4, 'AI Ahly SC', 'AFRICA'),
(5, 'Inter Miami CF', 'AMERICA');


SELECT setval(
    pg_get_serial_sequence('"Team"', 'id'),
    (SELECT MAX(id) FROM "Team")
);


INSERT INTO "Player" (id, name, age, "position", id_team)
VALUES
(1, 'Thibaut Courtois', 32, 'GK', 1),
(2, 'Dani Carvajal', 33, 'DEF', 1),
(3, 'Jude Bellingham', 21, 'MIDF', 1),
(4, 'Robert Lewandowski', 36, 'STR', 2),
(5, 'Antoine Griezmann', 33, 'STR', 3);


SELECT setval(
    pg_get_serial_sequence('"Player"', 'id'),
    (SELECT MAX(id) FROM "Player")
);

UPDATE "Player"
SET goal_nb = 0
WHERE id = 1;

UPDATE "Player"
SET goal_nb = 2
WHERE id = 2;

UPDATE "Player"
SET goal_nb = 5
WHERE id = 3;

UPDATE "Player"
SET goal_nb = null
WHERE id = 4;

UPDATE "Player"
SET goal_nb = null
WHERE id = 5;