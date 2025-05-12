package database;

import enums.PerformanceTypeEnum;
import objects.Student;
import util.Message;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Queue management.
 * Handles operations related to student queues.
 */
public class QueueDAO {
    private static final Logger logger = Logger.getLogger(QueueDAO.class.getName());
    private final PropertyChangeSupport support;

    // Message types
    public static final String QUEUE_UPDATED = "QUEUE_UPDATED";
    public static final String DATABASE_ERROR = "DATABASE_ERROR";

    /**
     * Creates a new QueueDAO instance.
     */
    public QueueDAO() {
        support = new PropertyChangeSupport(this);
    }

    /**
     * Gets all students in a queue for a specific performance type.
     *
     * @param performanceType Performance type to get queue for
     * @return List of students in the queue
     * @throws SQLException If a database error occurs
     */
    public List<Student> getQueueByPerformanceType(PerformanceTypeEnum performanceType) throws SQLException {
        List<Student> studentsInQueue = new ArrayList<>();
        String sql = "SELECT s.* FROM Student s " +
                "JOIN QueueEntry q ON s.via_id = q.student_via_id " +
                "WHERE q.performance_type = ? " +
                "ORDER BY q.entry_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, performanceType.name());

            try (ResultSet rs = stmt.executeQuery()) {
                StudentDAO studentDAO = new StudentDAO();
                while (rs.next()) {
                    Student student = studentDAO.mapResultSetToStudent(rs);
                    studentsInQueue.add(student);
                }
            }
        } catch (SQLException e) {
            handleSQLException("Error getting queue for " + performanceType, e);
            throw e;
        }

        return studentsInQueue;
    }

    /**
     * Adds a student to a queue.
     *
     * @param studentId Student VIA ID
     * @param performanceType Queue performance type
     * @return True if successfully added
     * @throws SQLException If a database error occurs
     */
    public boolean addToQueue(int studentId, PerformanceTypeEnum performanceType) throws SQLException {
        String sql = "INSERT INTO QueueEntry (student_via_id, performance_type, entry_date) " +
                "VALUES (?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setString(2, performanceType.name());

            int affectedRows = stmt.executeUpdate();

            boolean success = affectedRows > 0;
            if (success) {
                // Notify listeners
                Message message = new Message(QUEUE_UPDATED, performanceType);
                support.firePropertyChange(QUEUE_UPDATED, null, message);
            }

            return success;
        } catch (SQLException e) {
            handleSQLException("Error adding student to queue: " + studentId, e);
            throw e;
        }
    }

    /**
     * Removes a student from a queue.
     *
     * @param studentId Student VIA ID
     * @param performanceType Queue performance type
     * @return True if successfully removed
     * @throws SQLException If a database error occurs
     */
    public boolean removeFromQueue(int studentId, PerformanceTypeEnum performanceType) throws SQLException {
        String sql = "DELETE FROM QueueEntry WHERE student_via_id = ? AND performance_type = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setString(2, performanceType.name());

            int affectedRows = stmt.executeUpdate();

            boolean success = affectedRows > 0;
            if (success) {
                // Notify listeners
                Message message = new Message(QUEUE_UPDATED, performanceType);
                support.firePropertyChange(QUEUE_UPDATED, null, message);
            }

            return success;
        } catch (SQLException e) {
            handleSQLException("Error removing student from queue: " + studentId, e);
            throw e;
        }
    }

    /**
     * Gets the count of students in a queue.
     *
     * @param performanceType Performance type of the queue
     * @return Number of students in the queue
     * @throws SQLException If a database error occurs
     */
    public int getQueueSize(PerformanceTypeEnum performanceType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM QueueEntry WHERE performance_type = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, performanceType.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            handleSQLException("Error getting queue size for " + performanceType, e);
            throw e;
        }
    }

    /**
     * Handles SQL exceptions by logging them and notifying listeners.
     *
     * @param message Error message
     * @param e SQLException to handle
     */
    private void handleSQLException(String message, SQLException e) {
        logger.log(Level.SEVERE, message + ": " + e.getMessage(), e);

        // Send message about database error
        Message errorMsg = new Message(DATABASE_ERROR, message + ": " + e.getMessage());
        support.firePropertyChange(DATABASE_ERROR, null, errorMsg);
    }

    /**
     * Adds a listener for DAO events.
     *
     * @param listener Listener to add
     */
    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener Listener to remove
     */
    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /**
     * Adds a listener for specific events.
     *
     * @param propertyName Property name to listen for
     * @param listener Listener to add
     */
    public void addListener(String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Removes a listener for specific events.
     *
     * @param propertyName Property name to stop listening for
     * @param listener Listener to remove
     */
    public void removeListener(String propertyName, PropertyChangeListener listener) {
        support.removePropertyChangeListener(propertyName, listener);
    }
}
