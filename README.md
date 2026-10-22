
# Library Book Management System

A desktop-based Library Book Management System built using Java Swing and MySQL.

## Features
- Add and manage books
- Register library members
- Issue and return books
- Track available book copies
- View library reports

## Technologies Used
- Java 21
- Java Swing
- MySQL
- JDBC
- MySQL Connector/J

## Project Structure
- `src/` - Java source code
- `sql/` - Database setup script
- `lib/` - MySQL JDBC connector
- `out/` - Compiled Java files (not uploaded)

## Setup
1. Install Java 21 and MySQL.
2. Run `sql/library_db.sql` in MySQL Workbench.
3. Set the `LIBRARY_DB_PASSWORD` environment variable to your MySQL password.
4. Open a terminal in the project folder and run:

```powershell
javac -d out -cp "lib\*" src\*.java
java -cp "out;lib\*" Main
```
