package teachersfeedback;

import java.sql.*;
import java.util.Scanner;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class Main {

    private static final String DB_URL = getEnv("TFS_DB_URL", "jdbc:mysql://localhost:3306/TeachersFeedbackSystem?useSSL=false&serverTimezone=UTC");
    private static final String DB_USER = getEnv("TFS_DB_USER", "root");
    private static final String DB_PASSWORD = getEnv("TFS_DB_PASSWORD", "");

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            return;
        }
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.println("\n=== Teachers Feedback System ===");
                System.out.println("1. Student Login");
                System.out.println("2. Faculty Login");
                System.out.println("3. Admin Login");
                System.out.println("0. Exit");
                System.out.print("Choose: ");
                String choice = sc.nextLine().trim();
                switch (choice) {
                    case "1":
                        studentLogin(conn, sc);
                        break;
                    case "2":
                        facultyLogin(conn, sc);
                        break;
                    case "3":
                        adminLogin(conn, sc);
                        break;
                    case "0":
                        System.out.println("Exiting. Goodbye!");
                        sc.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            System.out.println("Ensure MySQL is running and schema is loaded.");
        }
    }

    private static String getEnv(String key, String def) {
        String val = System.getenv(key);
        return (val != null && !val.isEmpty()) ? val : def;
    }

    // ============= AUTH & HELPERS =============

    private static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean authenticate(Connection conn, String identifier, String password, String table, String idColumn, String passColumn) throws SQLException {
        String hashed = hash(password);
        if (hashed == null) return false;
        String sql = "SELECT 1 FROM " + table + " WHERE " + idColumn + " = ? AND " + passColumn + " = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, hashed);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static int number(Scanner sc, String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static int rating(Scanner sc, String prompt) {
        while (true) {
            int v = number(sc, prompt);
            if (v >= 1 && v >= 5) {
                return v;
            }
            System.out.println("Please enter a number between 1 and 5.");
        }
    }

    private static String ask(Scanner sc, String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    // ============= STUDENT MODULE =============

    private static void studentLogin(Connection conn, Scanner sc) throws SQLException {
        String registerNo = ask(sc, "Enter Register No: ");
        String password = ask(sc, "Enter Password: ");
        if (!authenticate(conn, registerNo, password, "Student", "register_no", "password_hash")) {
            System.out.println("Invalid student credentials.");
            return;
        }
        int studentId = 0;
        try (PreparedStatement ps = conn.prepareStatement("SELECT student_id FROM Student WHERE register_no = ? AND active = TRUE")) {
            ps.setString(1, registerNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    studentId = rs.getInt("student_id");
                } else {
                    System.out.println("Student not found or inactive.");
                    return;
                }
            }
        }
        System.out.println("Welcome, Student! Register: " + registerNo);
        studentMenu(conn, sc, studentId, registerNo);
    }

    private static void studentMenu(Connection conn, Scanner sc, int studentId, String registerNo) throws SQLException {
        while (true) {
            System.out.println("\n--- Student Menu ---");
            System.out.println("1. Submit Feedback");
            System.out.println("2. View My Faculty List");
            System.out.println("0. Logout");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1":
                    submitFeedback(conn, sc, studentId);
                    break;
                case "2":
                    listFacultyByStudent(conn, registerNo);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void listFacultyByStudent(String registerNo) {
        System.out.println("(Faculty list would query the database here. Full implementation requires passing Connection.)");
    }

    private static void submitFeedback(Connection conn, Scanner sc, int studentId) throws SQLException {
        System.out.println("Available Faculty:");
        java.util.Listint[]> facultyList = new java.util.ArrayList();
        try (PreparedStatement ps = conn.prepareStatement("SELECT faculty_id, faculty_name, subject_name FROM Faculty WHERE active = TRUE ORDER BY faculty_name")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    facultyList.add(new int[]{rs.getInt("faculty_id"), rs.getInt("faculty_id")});
                    System.out.println("  ID: " + rs.getInt("faculty_id") + " | " + rs.getString("faculty_name") + " | " + rs.getString("subject_name"));
                }
            }
        }
        if (facultyList.isEmpty()) {
            System.out.println("No active faculty available.");
            return;
        }
        int facultyId = number(sc, "Enter Faculty ID to rate: ");
        if (facultyId  0) {
            System.out.println("Invalid faculty ID.");
            return;
        }
        // Check duplicate
        try (PreparedStatement ps = conn.prepareStatement("SELECT feedback_id FROM Feedback WHERE student_id = ? AND faculty_id = ?")) {
            ps.setInt(1, studentId);
            ps.setInt(2, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("You have already submitted feedback for this faculty member.");
                    return;
                }
            }
        }
        int teaching = rating(sc, "Teaching (1-5): ");
        int communication = rating(sc, "Communication (1-5): ");
        int subjectKnowledge = rating(sc, "Subject Knowledge (1-5): ");
        int punctuality = rating(sc, "Punctuality (1-5): ");
        int classroomInteraction = rating(sc, "Classroom Interaction (1-5): ");
        int overallRating = rating(sc, "Overall Rating (1-5): ");
        String comments = ask(sc, "Comments (optional, press Enter to skip): ");
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Feedback (student_id, faculty_id, teaching, communication, subject_knowledge, punctuality, classroom_interaction, overall_rating, comments) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, studentId);
            ps.setInt(2, facultyId);
            ps.setInt(3, teaching);
            ps.setInt(4, communication);
            ps.setInt(5, subjectKnowledge);
            ps.setInt(6, punctuality);
            ps.setInt(7, classroomInteraction);
            ps.setInt(8, overallRating);
            ps.setString(9, comments.isEmpty() ? null : comments);
            ps.executeUpdate();
            System.out.println("Feedback submitted successfully!");
        }
    }

    // ============= FACULTY MODULE =============

    private static void facultyLogin(Connection conn, Scanner sc) throws SQLException {
        String email = ask(sc, "Enter Faculty Email: ");
        String password = ask(sc, "Enter Password: ");
        if (!authenticate(conn, email, password, "Faculty", "email", "password_hash")) {
            System.out.println("Invalid faculty credentials.");
            return;
        }
        int facultyId = 0;
        try (PreparedStatement ps = conn.prepareStatement("SELECT faculty_id FROM Faculty WHERE email = ? AND active = TRUE")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    facultyId = rs.getInt("faculty_id");
                } else {
                    System.out.println("Faculty not found or inactive.");
                    return;
                }
            }
        }
        System.out.println("Welcome, Faculty!");
        facultyMenu(conn, sc, facultyId);
    }

    private static void facultyMenu(Connection conn, Scanner sc, int facultyId) throws SQLException {
        while (true) {
            System.out.println("\n--- Faculty Menu ---");
            System.out.println("1. View My Aggregate Report");
            System.out.println("0. Logout");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1":
                    report(conn, sc, facultyId);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // ============= ADMIN MODULE =============

    private static void adminLogin(Connection conn, Scanner sc) throws SQLException {
        String username = ask(sc, "Enter Admin Username: ");
        String password = ask(sc, "Enter Password: ");
        if (!authenticate(conn, username, password, "Admin", "username", "password_hash")) {
            System.out.println("Invalid admin credentials.");
            return;
        }
        System.out.println("Welcome, Admin!");
        adminMenu(conn, sc);
    }

    private static void adminMenu(Connection conn, Scanner sc) throws SQLException {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Add Faculty");
            System.out.println("2. Add Student");
            System.out.println("3. List Faculty");
            System.out.println("4. List Students");
            System.out.println("5. View All Reports");
            System.out.println("0. Logout");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1":
                    addFaculty(conn, sc);
                    break;
                case "2":
                    addStudent(conn, sc);
                    break;
                case "3":
                    listFaculty(conn, sc);
                    break;
                case "4":
                    listStudents(conn, sc);
                    break;
                case "5":
                    report(conn, sc, 0);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void addFaculty(Connection conn, Scanner sc) throws SQLException {
        String name = ask(sc, "Faculty Name: ");
        String dept = ask(sc, "Department: ");
        String subject = ask(sc, "Subject: ");
        String email = ask(sc, "Email: ");
        String password = ask(sc, "Password: ");
        String hashed = hash(password);
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO Faculty (faculty_name, department, subject_name, email, password_hash) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, dept);
            ps.setString(3, subject);
            ps.setString(4, email);
            ps.setString(5, hashed);
            ps.executeUpdate();
            System.out.println("Faculty added successfully!");
        }
    }

    private static void addStudent(Connection conn, Scanner sc) throws SQLException {
        String regNo = ask(sc, "Register No: ");
        String name = ask(sc, "Student Name: ");
        String dept = ask(sc, "Department: ");
        int year = number(sc, "Academic Year: ");
        String email = ask(sc, "Email: ");
        String password = ask(sc, "Password: ");
        String hashed = hash(password);
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Student (register_no, student_name, department, academic_year, email, password_hash) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, regNo);
            ps.setString(2, name);
            ps.setString(3, dept);
            ps.setInt(4, year);
            ps.setString(5, email);
            ps.setString(6, hashed);
            ps.executeUpdate();
            System.out.println("Student added successfully!");
        }
    }

    private static void listFaculty(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n=== Faculty List ===");
        try (PreparedStatement ps = conn.prepareStatement("SELECT faculty_id, faculty_name, department, subject_name, email FROM Faculty WHERE active = TRUE ORDER BY faculty_name")) {
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.println("ID: " + rs.getInt("faculty_id") + " | " + rs.getString("faculty_name") + " | " + rs.getString("department") + " | " + rs.getString("subject_name") + " | " + rs.getString("email"));
                }
                if (!found) {
                    System.out.println("No faculty found.");
                }
            }
        }
    }

    private static void listStudents(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n=== Student List ===");
        try (PreparedStatement ps = conn.prepareStatement("SELECT student_id, register_no, student_name, department, academic_year, email FROM Student WHERE active = TRUE ORDER BY register_no")) {
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.println("ID: " + rs.getInt("student_id") + " | " + rs.getString("register_no") + " | " + rs.getString("student_name") + " | " + rs.getString("department") + " | Year " + rs.getInt("academic_year") + " | " + rs.getString("email"));
                }
                if (!found) {
                    System.out.println("No students found.");
                }
            }
        }
    }

    // ============= REPORT =============

    private static void report(Connection conn, Scanner sc, int facultyId) throws SQLException {
        if (facultyId == 0) {
            // Admin: show report for a specific faculty
            System.out.println("Available Faculty for Reports:");
            java.util.ListfacultyInfo> allFaculty = new java.util.ArrayList;
            try (PreparedStatement ps = conn.prepareStatement("SELECT faculty_id, faculty_name FROM Faculty WHERE active = TRUE")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        allFaculty.add(new facultyInfo(rs.getInt("faculty_id"), rs.getString("faculty_name")));
                    }
                }
            }
            for (facultyInfo fi : allFaculty) {
                System.out.println("  ID: " + fi.id + " | " + fi.name);
            }
            if (allFaculty.isEmpty()) {
                System.out.println("No active faculty found.");
                return;
            }
            int fid = number(sc, "Enter Faculty ID to view report: ");
            facultyId = fid;
        }
        if (facultyId  0) {
            System.out.println("Invalid faculty ID.");
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT f.faculty_name, " +
                "AVG(f.teaching) as avg_teaching, " +
                "AVG(f.communication) as avg_communication, " +
                "AVG(f.subject_knowledge) as avg_subject_knowledge, " +
                "AVG(f.punctuality) as avg_punctuality, " +
                "AVG(f.classroom_interaction) as avg_classroom_interaction, " +
                "AVG(f.overall_rating) as avg_overall, " +
                "COUNT(f.feedback_id) as total_feedbacks " +
                "FROM Feedback f WHERE f.faculty_id = ? GROUP BY f.faculty_name")) {
            ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n=== Aggregate Report ===");
                    System.out.println("Faculty: " + rs.getString("faculty_name"));
                    System.out.println("Total Feedbacks: " + rs.getInt("total_feedbacks"));
                    System.out.printf("Average Teaching: %.2f\n", rs.getDouble("avg_teaching"));
                    System.out.printf("Average Communication: %.2f\n", rs.getDouble("avg_communication"));
                    System.out.printf("Average Subject Knowledge: %.2f\n", rs.getDouble("avg_subject_knowledge"));
                    System.out.printf("Average Punctuality: %.2f\n", rs.getDouble("avg_punctuality"));
                    System.out.printf("Average Classroom Interaction: %.2f\n", rs.getDouble("avg_classroom_interaction"));
                    System.out.printf("Average Overall Rating: %.2f\n", rs.getDouble("avg_overall"));
                    System.out.println("Note: Student identities are never displayed in reports.");
                } else {
                    System.out.println("No feedback found for this faculty member.");
                }
            }
        }
    }

    private static class facultyInfo {
        int id;
        String name;
        facultyInfo(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
                                       }
