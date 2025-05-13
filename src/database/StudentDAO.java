package database;

import enums.PerformanceTypeEnum;
import objects.Student;
import util.Message;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Student entiteter.
 * Implementerer GenericDAO for standardiserede databaseoperationer.
 */
public class StudentDAO implements GenericDAO<Student, Integer> {
    private static final Logger logger = Logger.getLogger(StudentDAO.class.getName());
    private final PropertyChangeSupport support;

    // Besked typer
    public static final String STUDENT_CREATED = "STUDENT_CREATED";
    public static final String STUDENT_UPDATED = "STUDENT_UPDATED";
    public static final String STUDENT_DELETED = "STUDENT_DELETED";
    public static final String DATABASE_ERROR = "DATABASE_ERROR";

    /**
     * Opretter en ny StudentDAO instans.
     */
    public StudentDAO() {
        this.support = new PropertyChangeSupport(this);
    }

    /**
     * Henter alle studerende fra databasen.
     *
     * @return Liste af studerende
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public List<Student> getAll() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT via_id, name, degree_end_date, degree_title, email, phone_number, " +
                "performance_needed FROM Student";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Student student = mapResultSetToStudent(rs);
                students.add(student);
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved hentning af alle studerende", e);
            throw e;
        }
        return students;
    }

    /**
     * Henter en student baseret på VIA ID.
     *
     * @param id Student VIA ID
     * @return Student objekt eller null hvis ikke fundet
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public Student getById(Integer id) throws SQLException {
        String sql = "SELECT via_id, name, degree_end_date, degree_title, email, phone_number, " +
                "performance_needed FROM Student WHERE via_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved hentning af student med ID " + id, e);
            throw e;
        }
        return null;
    }

    @Override
    public boolean insert(Student student) throws SQLException {
        String sql = "INSERT INTO Student (via_id, name, degree_end_date, degree_title, email, phone_number, " +
                "performance_needed) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, student.getViaId());
            stmt.setString(2, student.getName());
            stmt.setDate(3, new java.sql.Date(student.getDegreeEndDate().getTime()));
            stmt.setString(4, student.getDegreeTitle());
            stmt.setString(5, student.getEmail());
            stmt.setLong(6, student.getPhoneNumber());
            stmt.setString(7, student.getPerformanceNeeded().name());

            int affectedRows = stmt.executeUpdate();

            boolean success = affectedRows > 0;
            if (success) {
                logger.info("Student [" + student.getName() + ", VIA ID: " + student.getViaId() + "] oprettet i database");

                // Send besked om at student er oprettet
                Message message = new Message(STUDENT_CREATED, student);
                support.firePropertyChange(STUDENT_CREATED, null, message);
            } else {
                logger.warning("Kunne ikke oprette student i database: " + student.getViaId());
            }

            return success;
        } catch (SQLException e) {
            // Check for violation of unique constraints
            if (e.getMessage().contains("student_email_key")) {
                // Email already exists
                String errorMsg = "Email " + student.getEmail() + " er allerede i brug";
                handleSQLException(errorMsg, e);
                throw new SQLException(errorMsg);
            } else if (e.getMessage().contains("student_pkey") || e.getMessage().contains("student_via_id_key")) {
                // VIA ID already exists
                String errorMsg = "VIA ID " + student.getViaId() + " er allerede i brug";
                handleSQLException(errorMsg, e);
                throw new SQLException(errorMsg);
            } else {
                // Other SQL error
                handleSQLException("Fejl ved indsættelse af student: " + student.getViaId(), e);
                throw e;
            }
        }
    }

    /**
     * Opdaterer en eksisterende student.
     *
     * @param student Student objekt med opdaterede oplysninger
     * @return true hvis operationen lykkedes
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public boolean update(Student student) throws SQLException {
        String sql = "UPDATE Student SET name = ?, degree_end_date = ?, degree_title = ?, email = ?, " +
                "phone_number = ?, performance_needed = ? WHERE via_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student.getName());
            stmt.setDate(2, new java.sql.Date(student.getDegreeEndDate().getTime()));
            stmt.setString(3, student.getDegreeTitle());
            stmt.setString(4, student.getEmail());
            stmt.setLong(5, student.getPhoneNumber());
            stmt.setString(6, student.getPerformanceNeeded().name());
            stmt.setInt(7, student.getViaId());

            int affectedRows = stmt.executeUpdate();

            boolean success = affectedRows > 0;
            if (success) {
                logger.info("Student [" + student.getName() + ", VIA ID: " + student.getViaId() + "] opdateret i database");

                // Send besked om at student er opdateret
                Message message = new Message(STUDENT_UPDATED, student);
                support.firePropertyChange(STUDENT_UPDATED, null, message);
            } else {
                logger.warning("Kunne ikke opdatere student i database: " + student.getViaId());
            }

            return success;
        } catch (SQLException e) {
            handleSQLException("Fejl ved opdatering af student: " + student.getViaId(), e);
            throw e;
        }
    }

    /**
     * Sletter en student fra databasen.
     *
     * @param id Student VIA ID
     * @return true hvis operationen lykkedes
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public boolean delete(Integer id) throws SQLException {
        // Først hent studenten så vi kan sende event efter sletning
        Student student = getById(id);
        if (student == null) {
            return false;
        }

        String sql = "DELETE FROM Student WHERE via_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();

            boolean success = affectedRows > 0;
            if (success) {
                logger.info("Student [VIA ID: " + id + "] slettet fra database");

                // Send besked om at student er slettet
                Message message = new Message(STUDENT_DELETED, student);
                support.firePropertyChange(STUDENT_DELETED, null, message);
            } else {
                logger.warning("Kunne ikke slette student fra database: " + id);
            }

            return success;
        } catch (SQLException e) {
            handleSQLException("Fejl ved sletning af student: " + id, e);
            throw e;
        }
    }

    /**
     * Tæller antal studerende i databasen.
     *
     * @return Antal studerende
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Student";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            handleSQLException("Fejl ved optælling af studerende", e);
            throw e;
        }
    }

    /**
     * Tjekker om en student eksisterer i databasen.
     *
     * @param id Student VIA ID
     * @return true hvis studenten findes
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public boolean exists(Integer id) throws SQLException {
        String sql = "SELECT 1 FROM Student WHERE via_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved tjek af students eksistens: " + id, e);
            throw e;
        }
    }

    /**
     * Finder studerende baseret på en performance type.
     *
     * @param performanceType Performance type at søge efter
     * @return Liste af studerende med den angivne performance type
     * @throws SQLException hvis der er problemer med databasen
     */
    public List<Student> getByPerformanceType(PerformanceTypeEnum performanceType) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT via_id, name, degree_end_date, degree_title, email, phone_number, " +
                "performance_needed FROM Student WHERE performance_needed = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, performanceType.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Student student = mapResultSetToStudent(rs);
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved hentning af studerende med performance type " + performanceType, e);
            throw e;
        }

        return students;
    }

    /**
     * Konverterer ResultSet til Student objekt.
     *
     * @param rs ResultSet at konvertere
     * @return Student objektet
     * @throws SQLException hvis der er problemer med databasen
     */
    public Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        int viaId = rs.getInt("via_id");
        String name = rs.getString("name");
        java.util.Date degreeEndDate = rs.getDate("degree_end_date");
        String degreeTitle = rs.getString("degree_title");
        String email = rs.getString("email");
        int phoneNumber = rs.getInt("phone_number");
        String performanceNeededStr = rs.getString("performance_needed");

        // Konverter strengværdien fra databasen til PerformanceTypeEnum
        PerformanceTypeEnum performanceNeeded;
        try {
            performanceNeeded = PerformanceTypeEnum.valueOf(performanceNeededStr);
        } catch (IllegalArgumentException e) {
            logger.warning("Ukendt performance type i databasen: " + performanceNeededStr + ". Bruger LOW som standard.");
            performanceNeeded = PerformanceTypeEnum.LOW; // Standard værdi hvis konvertering fejler
        }

        // Opret student-objekt uden hasLaptop
        return new Student(name, degreeEndDate, degreeTitle, viaId, email, phoneNumber, performanceNeeded);
    }

    /**
     * Håndterer SQLException ved at logge den og sende en besked om fejlen.
     *
     * @param message Fejlbeskeden
     * @param e SQLException undtagelsen
     */
    private void handleSQLException(String message, SQLException e) {
        logger.log(Level.SEVERE, message + ": " + e.getMessage(), e);

        // Send besked om databasefejl
        Message errorMsg = new Message(DATABASE_ERROR,
                message + ": " + e.getMessage());
        support.firePropertyChange(DATABASE_ERROR, null, errorMsg);
    }

    /**
     * Tilføj en lytter til DAO-hændelser.
     *
     * @param listener PropertyChangeListener der skal tilføjes
     */
    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Fjern en lytter til DAO-hændelser.
     *
     * @param listener PropertyChangeListener der skal fjernes
     */
    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /**
     * Tilføj en lytter til specifikke DAO-hændelser.
     *
     * @param propertyName Navnet på egenskaben at lytte efter
     * @param listener PropertyChangeListener der skal tilføjes
     */
    public void addListener(String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Fjern en lytter til specifikke DAO-hændelser.
     *
     * @param propertyName Navnet på egenskaben at fjerne lytter fra
     * @param listener PropertyChangeListener der skal fjernes
     */
    public void removeListener(String propertyName, PropertyChangeListener listener) {
        support.removePropertyChangeListener(propertyName, listener);
    }
}