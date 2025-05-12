package database;

import enums.ReservationStatusEnum;
import objects.Laptop;
import objects.Reservation;
import objects.Student;
import util.Message;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Reservation entiteter.
 * Implementerer GenericDAO for standardiserede databaseoperationer.
 */
public class ReservationDAO implements GenericDAO<Reservation, UUID> {
    private static final Logger logger = Logger.getLogger(ReservationDAO.class.getName());
    private final PropertyChangeSupport support;

    // Dependencies
    private final LaptopDAO laptopDAO;
    private final StudentDAO studentDAO;

    // Besked typer
    public static final String RESERVATION_CREATED = "RESERVATION_CREATED";
    public static final String RESERVATION_UPDATED = "RESERVATION_UPDATED";
    public static final String RESERVATION_DELETED = "RESERVATION_DELETED";
    public static final String RESERVATION_STATUS_CHANGED = "RESERVATION_STATUS_CHANGED";
    public static final String DATABASE_ERROR = "DATABASE_ERROR";

    /**
     * Opretter en ny ReservationDAO instans.
     */
    public ReservationDAO() {
        this.support = new PropertyChangeSupport(this);
        this.laptopDAO = new LaptopDAO();
        this.studentDAO = new StudentDAO();
    }

    /**
     * Henter alle reservationer fra databasen.
     *
     * @return Liste af reservationer
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public List<Reservation> getAll() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT reservation_uuid, laptop_uuid, student_via_id, status, creation_date " +
                "FROM Reservation";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Reservation reservation = mapResultSetToReservation(rs);
                if (reservation != null) {
                    reservations.add(reservation);
                }
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved hentning af alle reservationer", e);
            throw e;
        }
        return reservations;
    }

    /**
     * Henter alle aktive reservationer.
     *
     * @return Liste af aktive reservationer
     * @throws SQLException hvis der er problemer med databasen
     */
    public List<Reservation> getAllActive() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT reservation_uuid, laptop_uuid, student_via_id, status, creation_date " +
                "FROM Reservation WHERE status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ReservationStatusEnum.ACTIVE.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = mapResultSetToReservation(rs);
                    if (reservation != null) {
                        reservations.add(reservation);
                    }
                }
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved hentning af aktive reservationer", e);
            throw e;
        }
        return reservations;
    }

    /**
     * Henter en specifik reservation baseret på UUID.
     *
     * @param id Reservation UUID
     * @return Reservation objekt eller null hvis ikke fundet
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public Reservation getById(UUID id) throws SQLException {
        String sql = "SELECT reservation_uuid, laptop_uuid, student_via_id, status, creation_date " +
                "FROM Reservation WHERE reservation_uuid = CAST(? AS UUID)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReservation(rs);
                }
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved hentning af reservation med ID " + id, e);
            throw e;
        }
        return null;
    }

    /**
     * Indsætter en ny reservation i databasen.
     *
     * @param reservation Reservation objekt
     * @return true hvis operationen lykkedes
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public boolean insert(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO Reservation (reservation_uuid, laptop_uuid, student_via_id, status, creation_date) " +
                "VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reservation.getReservationId().toString());
            stmt.setString(2, reservation.getLaptop().getId().toString());
            stmt.setInt(3, reservation.getStudent().getViaId());
            stmt.setString(4, reservation.getStatus().name());
            stmt.setTimestamp(5, new Timestamp(reservation.getCreationDate().getTime()));

            int affectedRows = stmt.executeUpdate();

            boolean success = affectedRows > 0;
            if (success) {
                logger.info("Reservation [ID: " + reservation.getReservationId() + "] oprettet: " +
                        reservation.getStudent().getName() + " -> " +
                        reservation.getLaptop().getBrand() + " " + reservation.getLaptop().getModel());

                // Send besked om at reservation er oprettet
                Message message = new Message(RESERVATION_CREATED, reservation);
                support.firePropertyChange(RESERVATION_CREATED, null, message);
            } else {
                logger.warning("Kunne ikke oprette reservation i database: " + reservation.getReservationId());
            }

            return success;
        } catch (SQLException e) {
            handleSQLException("Fejl ved indsættelse af reservation: " + reservation.getReservationId(), e);
            throw e;
        }
    }

    /**
     * Opretter reservation med transaktionssupport, der også opdaterer laptop-tilstand.
     *
     * @param reservation Reservationsobjekt at oprette
     * @return true hvis operationen lykkedes
     * @throws SQLException hvis der er problemer med databasen
     */
    public boolean createReservationWithTransaction(Reservation reservation) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Indsæt reservation
            String sql = "INSERT INTO Reservation (reservation_uuid, laptop_uuid, student_via_id, status, creation_date) " +
                    "VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, reservation.getReservationId().toString());
                stmt.setString(2, reservation.getLaptop().getId().toString());
                stmt.setInt(3, reservation.getStudent().getViaId());
                stmt.setString(4, reservation.getStatus().name());
                stmt.setTimestamp(5, new Timestamp(reservation.getCreationDate().getTime()));
                stmt.executeUpdate();
            }

            // 2. Opdater laptop tilstand til LoanedState
            sql = "UPDATE Laptop SET state = 'LoanedState' WHERE laptop_uuid = CAST(? AS UUID)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, reservation.getLaptop().getId().toString());
                stmt.executeUpdate();
            }

            // Commit transaktionen
            conn.commit();

            logger.info("Reservation [ID: " + reservation.getReservationId() + "] oprettet med transaktion: " +
                    reservation.getStudent().getName() + " -> " +
                    reservation.getLaptop().getBrand() + " " + reservation.getLaptop().getModel());

            // Send besked om at reservation er oprettet
            Message message = new Message(RESERVATION_CREATED, reservation);
            support.firePropertyChange(RESERVATION_CREATED, null, message);

            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warning("Transaktion rullet tilbage: " + e.getMessage());
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Fejl under rollback: " + ex.getMessage(), ex);
                }
            }
            handleSQLException("Fejl ved oprettelse af reservation med transaktion: " +
                    reservation.getReservationId(), e);
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.warning("Fejl ved nulstilling af forbindelse: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Opdaterer en eksisterende reservation.
     *
     * @param reservation Reservation objekt med opdaterede oplysninger
     * @return true hvis operationen lykkedes
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public boolean update(Reservation reservation) throws SQLException {
        String sql = "UPDATE Reservation SET status = ? WHERE reservation_uuid = CAST(? AS UUID)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reservation.getStatus().name());
            stmt.setString(2, reservation.getReservationId().toString());

            int affectedRows = stmt.executeUpdate();

            boolean success = affectedRows > 0;
            if (success) {
                logger.info("Reservation [ID: " + reservation.getReservationId() + "] opdateret til status: " +
                        reservation.getStatus().name());

                // Send besked om at reservation er opdateret
                Message message = new Message(RESERVATION_UPDATED, reservation);
                support.firePropertyChange(RESERVATION_UPDATED, null, message);
            } else {
                logger.warning("Kunne ikke opdatere reservation i database: " + reservation.getReservationId());
            }

            return success;
        } catch (SQLException e) {
            handleSQLException("Fejl ved opdatering af reservation: " + reservation.getReservationId(), e);
            throw e;
        }
    }

    /**
     * Opdaterer reservationsstatus med transaktion, der også opdaterer laptop-tilstand.
     *
     * @param reservation Reservationsobjekt med den nye status
     * @return true hvis operationen lykkedes
     * @throws SQLException hvis der er problemer med databasen
     */
    public boolean updateStatusWithTransaction(Reservation reservation) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Hent den nuværende status
            String selectSql = "SELECT status FROM Reservation WHERE reservation_uuid = CAST(? AS UUID)";
            ReservationStatusEnum currentStatus;
            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setString(1, reservation.getReservationId().toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return false; // Reservation findes ikke
                    }
                    currentStatus = ReservationStatusEnum.valueOf(rs.getString("status"));
                }
            }

            // 2. Opdater reservation status
            String updateSql = "UPDATE Reservation SET status = ? WHERE reservation_uuid = CAST(? AS UUID)";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, reservation.getStatus().name());
                stmt.setString(2, reservation.getReservationId().toString());
                stmt.executeUpdate();
            }

            // 3. Hvis status ændres fra Active til completed eller cancelled
            if (currentStatus == ReservationStatusEnum.ACTIVE &&
                    (reservation.getStatus() == ReservationStatusEnum.COMPLETED ||
                            reservation.getStatus() == ReservationStatusEnum.CANCELLED)) {

                // Opdater laptop tilstand til Available
                String laptopSql = "UPDATE Laptop SET state = 'AvailableState' WHERE laptop_uuid = CAST(? AS UUID)";
                try (PreparedStatement stmt = conn.prepareStatement(laptopSql)) {
                    stmt.setString(1, reservation.getLaptop().getId().toString());
                    stmt.executeUpdate();
                }
            }

            // Commit transaktionen
            conn.commit();

            logger.info("Reservation [ID: " + reservation.getReservationId() + "] status ændret fra " +
                    currentStatus.name() + " til " + reservation.getStatus().name());

            // Send besked om statusændring
            Message message = new Message(RESERVATION_STATUS_CHANGED,
                    new Object[]{reservation, currentStatus, reservation.getStatus()});
            support.firePropertyChange(RESERVATION_STATUS_CHANGED, null, message);

            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warning("Transaktion rullet tilbage: " + e.getMessage());
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Fejl under rollback: " + ex.getMessage(), ex);
                }
            }
            handleSQLException("Fejl ved opdatering af reservationsstatus med transaktion: " +
                    reservation.getReservationId(), e);
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.warning("Fejl ved nulstilling af forbindelse: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Sletter en reservation fra databasen.
     *
     * @param id Reservation UUID
     * @return true hvis operationen lykkedes
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public boolean delete(UUID id) throws SQLException {
        // Først hent reservationen så vi kan sende event efter sletning
        Reservation reservation = getById(id);
        if (reservation == null) {
            return false;
        }

        String sql = "DELETE FROM Reservation WHERE reservation_uuid = CAST(? AS UUID)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            int affectedRows = stmt.executeUpdate();

            boolean success = affectedRows > 0;
            if (success) {
                logger.info("Reservation [ID: " + id + "] slettet fra database");

                // Send besked om sletning
                Message message = new Message(RESERVATION_DELETED, reservation);
                support.firePropertyChange(RESERVATION_DELETED, null, message);
            } else {
                logger.warning("Kunne ikke slette reservation fra database: " + id);
            }

            return success;
        } catch (SQLException e) {
            handleSQLException("Fejl ved sletning af reservation: " + id, e);
            throw e;
        }
    }

    /**
     * Tæller antal reservationer i databasen.
     *
     * @return Antal reservationer
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Reservation";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            handleSQLException("Fejl ved optælling af reservationer", e);
            throw e;
        }
    }

    /**
     * Tæller antal aktive reservationer.
     *
     * @return Antal aktive reservationer
     * @throws SQLException hvis der er problemer med databasen
     */
    public int countActive() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Reservation WHERE status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ReservationStatusEnum.ACTIVE.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved optælling af aktive reservationer", e);
            throw e;
        }
    }

    /**
     * Tjekker om en reservation eksisterer i databasen.
     *
     * @param id Reservation UUID
     * @return true hvis reservationen findes
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public boolean exists(UUID id) throws SQLException {
        String sql = "SELECT 1 FROM Reservation WHERE reservation_uuid = CAST(? AS UUID)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved tjek af reservations eksistens: " + id, e);
            throw e;
        }
    }

    /**
     * Finder alle reservationer for en bestemt student.
     *
     * @param studentId Student VIA ID
     * @return Liste af reservationer for studenten
     * @throws SQLException hvis der er problemer med databasen
     */
    public List<Reservation> getByStudentId(int studentId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT reservation_uuid, laptop_uuid, student_via_id, status, creation_date " +
                "FROM Reservation WHERE student_via_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = mapResultSetToReservation(rs);
                    if (reservation != null) {
                        reservations.add(reservation);
                    }
                }
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved hentning af reservationer for student " + studentId, e);
            throw e;
        }
        return reservations;
    }

    /**
     * Finder alle reservationer for en bestemt laptop.
     *
     * @param laptopId Laptop UUID
     * @return Liste af reservationer for laptopen
     * @throws SQLException hvis der er problemer med databasen
     */
    public List<Reservation> getByLaptopId(UUID laptopId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT reservation_uuid, laptop_uuid, student_via_id, status, creation_date " +
                "FROM Reservation WHERE laptop_uuid = CAST(? AS UUID)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, laptopId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = mapResultSetToReservation(rs);
                    if (reservation != null) {
                        reservations.add(reservation);
                    }
                }
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved hentning af reservationer for laptop " + laptopId, e);
            throw e;
        }
        return reservations;
    }

    /**
     * Tjekker om en student har en aktiv reservation.
     *
     * @param studentId Student VIA ID
     * @return true hvis studenten har en aktiv reservation
     * @throws SQLException hvis der er problemer med databasen
     */
    public boolean studentHasActiveLaptop(int studentId) throws SQLException {
        String sql = "SELECT 1 FROM Reservation WHERE student_via_id = ? AND status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setString(2, ReservationStatusEnum.ACTIVE.name());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved tjek af students aktive reservation: " + studentId, e);
            throw e;
        }
    }

    /**
     * Finder alle studerende der har en aktiv reservation.
     *
     * @return Liste af studerende med aktive reservationer
     * @throws SQLException hvis der er problemer med databasen
     */
    public List<Student> getStudentsWithActiveLaptop() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.* FROM Student s " +
                "JOIN Reservation r ON s.via_id = r.student_via_id " +
                "WHERE r.status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ReservationStatusEnum.ACTIVE.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Student student = studentDAO.mapResultSetToStudent(rs);
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved hentning af studerende med aktive reservationer", e);
            throw e;
        }
        return students;
    }

    /**
     * Konverterer ResultSet til Reservation objekt.
     *
     * @param rs ResultSet at konvertere
     * @return Reservation objektet eller null hvis konvertering fejler
     * @throws SQLException hvis der er problemer med databasen
     */
    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        UUID reservationId = UUID.fromString(rs.getString("reservation_uuid"));
        UUID laptopId = UUID.fromString(rs.getString("laptop_uuid"));
        int studentId = rs.getInt("student_via_id");
        String statusStr = rs.getString("status");
        Timestamp creationTimestamp = rs.getTimestamp("creation_date");

        // Konverter strengværdien fra databasen til ReservationStatusEnum
        ReservationStatusEnum status;
        try {
            status = ReservationStatusEnum.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            logger.warning("Ukendt reservationsstatus i databasen: " + statusStr + ". Bruger ACTIVE som standard.");
            status = ReservationStatusEnum.ACTIVE; // Standard værdi hvis konvertering fejler
        }

        // Hent Laptop og Student objekter
        Laptop laptop;
        Student student;

        try {
            laptop = laptopDAO.getById(laptopId);
            student = studentDAO.getById(studentId);
        } catch (SQLException e) {
            logger.warning("Kunne ikke hente laptop eller student for reservation " + reservationId + ": " + e.getMessage());
            return null;
        }

        // Tjek at både laptop og student blev fundet
        if (laptop == null) {
            logger.warning("Laptop med ID " + laptopId + " blev ikke fundet for reservation " + reservationId);
            return null;
        }

        if (student == null) {
            logger.warning("Student med ID " + studentId + " blev ikke fundet for reservation " + reservationId);
            return null;
        }

        Date creationDate = creationTimestamp != null ? new Date(creationTimestamp.getTime()) : new Date();

        // Opret og returner nyt Reservation objekt
        return new Reservation(reservationId, student, laptop, status, creationDate);
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