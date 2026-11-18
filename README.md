# Teachers Feedback System

Java 17 console application using JDBC and MySQL, following the supplied Teachers Feedback System project brief.

## Modules
- Student login and feedback submission
- Faculty login and anonymized aggregate report viewing
- Admin login, student/faculty creation, lists and reports
- JDBC database connectivity and prepared statements
- 1-to-5 ratings for teaching, communication, subject knowledge, punctuality, classroom interaction and overall performance
- Database-level prevention of duplicate feedback from one student for the same faculty member

## Setup
1. Install JDK 17+, Maven 3.8+ and MySQL 8+.
2. Execute `database/schema.sql` in MySQL Workbench.
3. Set `TFS_DB_USER=root` and `TFS_DB_PASSWORD=your_password` in Windows CMD (or equivalent environment variables). Defaults are root and blank password for local lab setup.
4. Run `mvn clean compile` then `mvn exec:java`.

## Demo logins
- Admin: `admin` / `admin123`
- Student: `814725148004` / `student123`
- Faculty: `faculty1@example.edu` / `faculty123`

Change demo passwords before real use. SHA-256 is for this academic demo only; production systems should use BCrypt or Argon2, HTTPS and formal privacy controls.

## Team in supplied brief
- 814725148003 - ADITYA KRISHNAN A
- 814725148004 - AJAY PRABU R K
- 814725148062 - VENKATESH R P
