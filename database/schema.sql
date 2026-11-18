CREATE DATABASE IF NOT EXISTS TeachersFeedbackSystem;
USE TeachersFeedbackSystem;

CREATE TABLE IF NOT EXISTS Student (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    register_no VARCHAR(30) NOT NULL UNIQUE,
    student_name VARCHAR(100) NOT NULL,
    department VARCHAR(80) NOT NULL,
    academic_year INT NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash CHAR(64) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS Faculty (
    faculty_id INT PRIMARY KEY AUTO_INCREMENT,
    faculty_name VARCHAR(100) NOT NULL,
    department VARCHAR(80) NOT NULL,
    subject_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash CHAR(64) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS Admin (
    admin_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash CHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS Feedback (
    feedback_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    faculty_id INT NOT NULL,
    teaching TINYINT NOT NULL CHECK(teaching BETWEEN 1 AND 5),
    communication TINYINT NOT NULL CHECK(communication BETWEEN 1 AND 5),
    subject_knowledge TINYINT NOT NULL CHECK(subject_knowledge BETWEEN 1 AND 5),
    punctuality TINYINT NOT NULL CHECK(punctuality BETWEEN 1 AND 5),
    classroom_interaction TINYINT NOT NULL CHECK(classroom_interaction BETWEEN 1 AND 5),
    overall_rating TINYINT NOT NULL CHECK(overall_rating BETWEEN 1 AND 5),
    comments VARCHAR(1000),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(student_id) REFERENCES Student(student_id),
    FOREIGN KEY(faculty_id) REFERENCES Faculty(faculty_id),
    UNIQUE KEY uq_student_faculty(student_id,faculty_id)
);

INSERT IGNORE INTO Admin(username,password_hash) VALUES('admin',SHA2('admin123',256));

INSERT IGNORE INTO Student(register_no,student_name,department,academic_year,email,password_hash) VALUES('814725148004','Ajay Prabu R K','CSE',2,'ajay.student@example.edu',SHA2('student123',256));

INSERT IGNORE INTO Faculty(faculty_name,department,subject_name,email,password_hash) VALUES('Demo Faculty','CSE','Java Programming','faculty1@example.edu',SHA2('faculty123',256));
