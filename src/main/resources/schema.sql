CREATE TABLE IF NOT EXISTS users (
    id UUID DEFAULT RANDOM_UUID(),
    username VARCHAR(100),
    email VARCHAR(100),
    password VARCHAR(100),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS roles (
    id UUID DEFAULT RANDOM_UUID(),
    role VARCHAR(100),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users_roles (
    users_id UUID,
    roles_id UUID,
    PRIMARY KEY(users_id, roles_id),
    FOREIGN KEY (users_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (roles_id) REFERENCES roles(id) ON DELETE CASCADE
);