DROP ALL OBJECTS;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(512) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    description VARCHAR(255),
    requestor_id BIGINT,
    CONSTRAINT fk_requests_to_users FOREIGN KEY(requestor_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255),
    is_available BOOLEAN NOT NULL,
    owner_id BIGINT,
    request_id BIGINT,
    CONSTRAINT pk_item PRIMARY KEY (id),
    CONSTRAINT fk_items_to_users FOREIGN KEY(owner_id) REFERENCES users(id),
    CONSTRAINT fk_items_to_requests FOREIGN KEY(request_id) REFERENCES requests(id)
);


CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    start_date timestamp NOT NULL,
    end_date timestamp NOT NULL,
    item_id BIGINT NOT NULL,
    booker_id BIGINT NOT NULL,
    status VARCHAR(100) NOT NULL,
    CONSTRAINT pk_booking PRIMARY KEY (id),
    CONSTRAINT fk_bookings_to_item FOREIGN KEY(item_id) REFERENCES items(id),
    CONSTRAINT fk_bookings_to_booker FOREIGN KEY(booker_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    text VARCHAR(300),
    item_id BIGINT,
    author_id BIGINT,
    created timestamp,
    CONSTRAINT fk_comments_to_item FOREIGN KEY(item_id) REFERENCES items(id),
    CONSTRAINT fk_comments_to_author FOREIGN KEY(author_id) REFERENCES users(id)
);
