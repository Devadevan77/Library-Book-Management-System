TablesCREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

CREATE TABLE IF NOT EXISTS books (
    book_id          INT AUTO_INCREMENT PRIMARY KEY,
    title            VARCHAR(150) NOT NULL,
    author           VARCHAR(100) NOT NULL,
    isbn             VARCHAR(20)  UNIQUE,
    category         VARCHAR(50),
    total_copies     INT NOT NULL CHECK (total_copies >= 0),
    available_copies INT NOT NULL CHECK (available_copies >= 0),
    CHECK (available_copies <= total_copies)
);

CREATE TABLE IF NOT EXISTS members (
    member_id   INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100) UNIQUE,
    phone       VARCHAR(20),
    joined_date DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id        INT NOT NULL,
    member_id      INT NOT NULL,
    issue_date     DATE NOT NULL,
    due_date       DATE NOT NULL,
    return_date    DATE NULL,
    FOREIGN KEY (book_id)   REFERENCES books(book_id),
    FOREIGN KEY (member_id) REFERENCES members(member_id)
);
