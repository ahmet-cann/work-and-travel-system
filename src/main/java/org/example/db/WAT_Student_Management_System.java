package org.example.db;

import org.example.model.Employer;
import org.example.model.JobOffer;
import org.example.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WAT_Student_Management_System {

    public static Student login(String email, String password) {
        String query = "SELECT StudentID, FirstName, LastName, Major, email FROM STUDENT WHERE LOWER(email) = LOWER(?) AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("StudentID"),
                            rs.getString("FirstName"),
                            rs.getString("LastName"),
                            rs.getString("Major"),
                            rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean hasExistingApplication(int studentID) {
        String query = "SELECT COUNT(*) FROM APPLICATION WHERE StudentID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, studentID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Employer> getEmployers() {
        List<Employer> list = new ArrayList<>();
        String query = "SELECT DISTINCT e.EmployerID, e.CompanyName, e.ContactPerson, l.City, l.State " +
                "FROM EMPLOYER e " +
                "LEFT JOIN JOB_OFFER j ON e.EmployerID = j.EmployerID " +
                "LEFT JOIN LOCATION l ON j.LocationID = l.LocationID";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Employer(
                        rs.getInt("EmployerID"),
                        rs.getString("CompanyName"),
                        rs.getString("ContactPerson"),
                        rs.getString("City") != null ? rs.getString("City") : "Bilinmiyor",
                        rs.getString("State") != null ? rs.getString("State") : ""
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<JobOffer> getJobsByEmployer(int employerID) {
        List<JobOffer> list = new ArrayList<>();
        String query = "SELECT j.JobID, j.PositionTitle, j.HourlyWage, l.City, l.State, " +
                "CASE WHEN a.JobID IS NOT NULL THEN 1 ELSE 0 END AS IsTaken " +
                "FROM JOB_OFFER j " +
                "JOIN LOCATION l ON j.LocationID = l.LocationID " +
                "LEFT JOIN APPLICATION a ON j.JobID = a.JobID AND a.AppStatus = 'Accepted' " +
                "WHERE j.EmployerID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, employerID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new JobOffer(
                            rs.getInt("JobID"),
                            rs.getString("PositionTitle"),
                            rs.getDouble("HourlyWage"),
                            rs.getString("City"),
                            rs.getString("State"),
                            rs.getInt("IsTaken") == 1
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean applyToJob(int studentID, int jobID) {
        String checkQuery = "SELECT COUNT(*) FROM APPLICATION WHERE JobID = ? AND AppStatus = 'Accepted'";
        String insertQuery = "INSERT INTO APPLICATION (ApplicationID, AppDate, AppStatus, JobType, StudentID, JobID) " +
                "VALUES ((SELECT ISNULL(MAX(ApplicationID), 0) + 1 FROM APPLICATION), GETDATE(), 'Pending', 'Primary', ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement checkPs = conn.prepareStatement(checkQuery)) {
                checkPs.setInt(1, jobID);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback();
                        return false;
                    }
                }
            }
            try (PreparedStatement insertPs = conn.prepareStatement(insertQuery)) {
                insertPs.setInt(1, studentID);
                insertPs.setInt(2, jobID);
                insertPs.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getStudentDashboardSummary(int studentID) {
        StringBuilder sb = new StringBuilder();
        String query = "SELECT " +
                " v.Status AS VisaStatus, v.InterviewDate, " +
                " j.PositionTitle, j.HourlyWage, " +
                " h.Address AS HousingAddress, h.CostPerWeek AS HousingCost, " +
                " l.City, l.State " +
                "FROM STUDENT s " +
                "LEFT JOIN VISA_PROCESS v ON s.StudentID = v.StudentID " +
                "LEFT JOIN APPLICATION a ON s.StudentID = a.StudentID " +
                "LEFT JOIN JOB_OFFER j ON a.JobID = j.JobID " +
                "LEFT JOIN LOCATION l ON j.LocationID = l.LocationID " +
                "LEFT JOIN HOUSING h ON l.LocationID = h.LocationID " +
                "WHERE s.StudentID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, studentID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String visa = rs.getString("VisaStatus");
                    String interview = rs.getString("InterviewDate");
                    String position = rs.getString("PositionTitle");
                    double wage = rs.getDouble("HourlyWage");
                    String housing = rs.getString("HousingAddress");
                    double hCost = rs.getDouble("HousingCost");
                    String city = rs.getString("City");
                    String state = rs.getString("State");

                    sb.append("<html><body style='font-family:Segoe UI; font-size:12px; color:#1a1a2e;'>")
                            .append("<h2 style='color:#0f6e56; margin-bottom:5px;'>✈ VİZE DURUMU</h2>")
                            .append("<b>Durum:</b> ").append(visa != null ? visa : "Aksiyon Gerekiyor").append("<br>")
                            .append("<b>Mülakat Tarihi:</b> ").append(interview != null ? interview : "Randevu Alınmadı").append("<br><br>")
                            .append("<h2 style='color:#0f6e56; margin-bottom:5px;'>💼 İŞ YERİ VE ÜCRET DETAYI</h2>")
                            .append("<b>Pozisyon:</b> ").append(position != null ? position : "Seçilmedi").append("<br>")
                            .append("<b>Saatlik Ücret:</b> ").append(position != null ? "$" + String.format("%.2f", wage) + "/saat" : "-").append("<br><br>")
                            .append("<h2 style='color:#0f6e56; margin-bottom:5px;'>🏠 KONAKLAMA (HOUSING)</h2>")
                            .append("<b>Adres:</b> ").append(housing != null ? housing : "Atanmadı").append("<br>")
                            .append("<b>Haftalık Kira:</b> ").append(housing != null ? "$" + String.format("%.2f", hCost) : "-").append("<br>")
                            .append("<b>Konum:</b> ").append(city != null ? city + ", " + state : "-")
                            .append("</body></html>");
                } else {
                    return "<html><body>Kayıtlı süreç bilgisi bulunamadı.</body></html>";
                }
            }
        } catch (SQLException e) {
            return "<html><body>Hata oluştu: " + e.getMessage() + "</body></html>";
        }
        return sb.toString();
    }
}