INSERT INTO USERS (ID, USERNAME, PASSWORD, EMAIL, ROLE)
VALUES
    (200, 'leonardo', 'password123', 'leonardo@taskmanager.com', 'TASK-OWNER'),
    (201, 'michelangelo', 'password123', 'michelangelo@taskmanager.com', 'TASK-OWNER');

INSERT INTO Tasks (ID, TITLE, DESCRIPTION, DUE_DATE, STATUS, USER_ID) VALUES
(100, 'Wash the dishes', 'Description to wash the dishes', '2024-08-15T00:00:00', 'TODO', 200),
(101, 'Throw the garbage', 'Description to throw the garbage', '2024-08-20T00:00:00', 'TODO', 200),
(102, 'Do groceries', 'Description to do groceries', '2024-08-30T00:00:00', 'COMPLETED', 201);

INSERT INTO ROLE (ID, NAME)
VALUES
(500, 'ADMIN'),
(501, 'TASK-OWNER');

INSERT INTO USER_ROLE (USER_ID, ROLE_ID) VALUES (200, 501);
INSERT INTO USER_ROLE (USER_ID, ROLE_ID) VALUES (201, 501);


