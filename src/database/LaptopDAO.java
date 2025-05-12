package database;

import enums.PerformanceTypeEnum;
import objects.Laptop;
import objects.LaptopState;
import util.Message;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Laptop entiteter.
 * Implementerer GenericDAO for standardiserede databaseoperationer.
 */
public class LaptopDAO implements GenericDAO<Laptop, UUID> {
    private static final Logger logger = Logger.getLogger(LaptopDAO.class.getName());
    private final PropertyChangeSupport support;

    // Besked typer
    public static final String LAPTOP_CREATED = "LAPTOP_CREATED";
    public static final String LAPTOP_UPDATED = "LAPTOP_UPDATED";
    public static final String LAPTOP_DELETED = "LAPTOP_DELETED";
    public static final String LAPTOP_STATE_CHANGED = "LAPTOP_STATE_CHANGED";
    public static final String DATABASE_ERROR = "DATABASE_ERROR";

    /**
     * Opretter en ny LaptopDAO instans.
     */
    public LaptopDAO() {
        this.support = new PropertyChangeSupport(this);
    }

    /**
     * Henter alle laptops fra databasen.
     *
     * @return Liste af laptops
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public List<Laptop> getAll() throws SQLException {
        List<Laptop> laptops = new ArrayList<>();
        String sql = "SELECT laptop_uuid, brand, model, gigabyte, ram, performance_type, state FROM Laptop";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Laptop laptop = mapResultSetToLaptop(rs);
                laptops.add(laptop);
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved hentning af alle laptops", e);
            throw e;
        }
        return laptops;
    }

    /**
     * Henter alle laptops med en bestemt performance type og tilstand.
     *
     * @param performanceType Performance typen at filtrere efter
     * @return List af laptops med den angivne performance type
     * @throws SQLException hvis der er problemer med databasen
     */
    public List<Laptop> getAvailableLaptopsByPerformance(PerformanceTypeEnum performanceType) throws SQLException {
        List<Laptop> laptops = new ArrayList<>();
        String sql = "SELECT laptop_uuid, brand, model, gigabyte, ram, performance_type, state " +
                "FROM Laptop WHERE performance_type = ? AND state = 'AvailableState'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, performanceType.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Laptop laptop = mapResultSetToLaptop(rs);
                    laptops.add(laptop);
                }
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved hentning af tilgængelige laptops med performance type " + performanceType, e);
            throw e;
        }
        return laptops;
    }

    /**
     * Henter laptop baseret på UUID.
     *
     * @param id Laptop UUID
     * @return Laptop object eller null hvis ikke fundet
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public Laptop getById(UUID id) throws SQLException {
        String sql = "SELECT laptop_uuid, brand, model, gigabyte, ram, performance_type, state FROM Laptop WHERE laptop_uuid = CAST(? AS UUID)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLaptop(rs);
                }
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved hentning af laptop med ID " + id, e);
            throw e;
        }
        return null;
    }

    /**
     * Indsætter en ny laptop i databasen.
     *
     * @param laptop Laptop objekt
     * @return true hvis operationen lykkedes
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public boolean insert(Laptop laptop) throws SQLException {
        String sql = "INSERT INTO Laptop (laptop_uuid, brand, model, gigabyte, ram, performance_type, state) " +
                "VALUES (CAST(? AS UUID), ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, laptop.getId().toString());
            stmt.setString(2, laptop.getBrand());
            stmt.setString(3, laptop.getModel());
            stmt.setInt(4, laptop.getGigabyte());
            stmt.setInt(5, laptop.getRam());
            stmt.setString(6, laptop.getPerformanceType().name());
            stmt.setString(7, laptop.getStateClassSimpleName());

            int affectedRows = stmt.executeUpdate();

            boolean success = affectedRows > 0;
            if (success) {
                logger.info("Laptop [" + laptop.getBrand() + " " + laptop.getModel() +
                        ", ID: " + laptop.getId() + "] oprettet i database");

                // Send besked om at laptop er oprettet
                Message message = new Message(LAPTOP_CREATED, laptop);
                support.firePropertyChange(LAPTOP_CREATED, null, message);
            } else {
                logger.warning("Kunne ikke oprette laptop i database: " + laptop.getId());
            }

            return success;
        } catch (SQLException e) {
            handleSQLException("Fejl ved indsættelse af laptop: " + laptop.getId(), e);
            throw e;
        }
    }

    /**
     * Opdaterer en eksisterende laptop.
     *
     * @param laptop Laptop objekt med opdaterede oplysninger
     * @return true hvis operationen lykkedes
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public boolean update(Laptop laptop) throws SQLException {
        String sql = "UPDATE Laptop SET brand = ?, model = ?, gigabyte = ?, ram = ?, performance_type = ?, state = ? " +
                "WHERE laptop_uuid = CAST(? AS UUID)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, laptop.getBrand());
            stmt.setString(2, laptop.getModel());
            stmt.setInt(3, laptop.getGigabyte());
            stmt.setInt(4, laptop.getRam());
            stmt.setString(5, laptop.getPerformanceType().name());
            stmt.setString(6, laptop.getStateClassSimpleName());
            stmt.setString(7, laptop.getId().toString());

            int affectedRows = stmt.executeUpdate();

            boolean success = affectedRows > 0;
            if (success) {
                logger.info("Laptop [" + laptop.getBrand() + " " + laptop.getModel() +
                        ", ID: " + laptop.getId() + "] opdateret i database");

                // Send besked om at laptop er opdateret
                Message message = new Message(LAPTOP_UPDATED, laptop);
                support.firePropertyChange(LAPTOP_UPDATED, null, message);
            } else {
                logger.warning("Kunne ikke opdatere laptop i database: " + laptop.getId());
            }

            return success;
        } catch (SQLException e) {
            handleSQLException("Fejl ved opdatering af laptop: " + laptop.getId(), e);
            throw e;
        }
    }

    /**
     * Opdaterer kun en laptops tilstand i databasen.
     *
     * @param laptop Laptop objekt med den nye tilstand
     * @return true hvis operationen lykkedes
     * @throws SQLException hvis der er problemer med databasen
     */
    public boolean updateState(Laptop laptop) throws SQLException {
        String sql = "UPDATE Laptop SET state = ? WHERE laptop_uuid = CAST(? AS UUID)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, laptop.getStateClassSimpleName());
            stmt.setString(2, laptop.getId().toString());

            int affectedRows = stmt.executeUpdate();

            boolean success = affectedRows > 0;
            if (success) {
                logger.info("Laptop [" + laptop.getBrand() + " " + laptop.getModel() +
                        ", ID: " + laptop.getId() + "] tilstand ændret til " + laptop.getStateClassSimpleName());

                // Send besked om at laptop tilstand er ændret
                Message message = new Message(LAPTOP_STATE_CHANGED, laptop);
                support.firePropertyChange(LAPTOP_STATE_CHANGED, null, message);
            } else {
                logger.warning("Kunne ikke opdatere laptop tilstand i database: " + laptop.getId());
            }

            return success;
        } catch (SQLException e) {
            handleSQLException("Fejl ved opdatering af laptop tilstand: " + laptop.getId(), e);
            throw e;
        }
    }

    /**
     * Sletter en laptop fra databasen.
     *
     * @param id Laptop UUID
     * @return true hvis operationen lykkedes
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public boolean delete(UUID id) throws SQLException {
        // Først hent lapptoppen så vi kan sende event efter sletning
        Laptop laptop = getById(id);
        if (laptop == null) {
            return false;
        }

        String sql = "DELETE FROM Laptop WHERE laptop_uuid = CAST(? AS UUID)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            int affectedRows = stmt.executeUpdate();

            boolean success = affectedRows > 0;
            if (success) {
                logger.info("Laptop [ID: " + id + "] slettet fra database");

                // Send besked om at laptop er slettet
                Message message = new Message(LAPTOP_DELETED, laptop);
                support.firePropertyChange(LAPTOP_DELETED, null, message);
            } else {
                logger.warning("Kunne ikke slette laptop fra database: " + id);
            }

            return success;
        } catch (SQLException e) {
            handleSQLException("Fejl ved sletning af laptop: " + id, e);
            throw e;
        }
    }

    /**
     * Tæller antal laptops i databasen.
     *
     * @return Antal laptops
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Laptop";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            handleSQLException("Fejl ved optælling af laptops", e);
            throw e;
        }
    }

    /**
     * Tjekker om en laptop eksisterer i databasen.
     *
     * @param id Laptop UUID
     * @return true hvis laptopen findes
     * @throws SQLException hvis der er problemer med databasen
     */
    @Override
    public boolean exists(UUID id) throws SQLException {
        String sql = "SELECT 1 FROM Laptop WHERE laptop_uuid = CAST(? AS UUID)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved tjek af laptops eksistens: " + id, e);
            throw e;
        }
    }

    /**
     * Tæller antal laptops med en bestemt tilstand.
     *
     * @param state Tilstandsklassenavn at tælle
     * @return Antal laptops med den tilstand
     * @throws SQLException hvis der er problemer med databasen
     */
    public int countByState(String state) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Laptop WHERE state = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, state);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            handleSQLException("Fejl ved optælling af laptops med tilstand " + state, e);
            throw e;
        }
    }

    /**
     * Henter alle tilgængelige laptops fra databasen.
     *
     * @return Liste af tilgængelige laptops
     * @throws SQLException hvis der er problemer med databasen
     */
    public List<Laptop> getAvailableLaptops() throws SQLException {
        String sql = "SELECT laptop_uuid, brand, model, gigabyte, ram, performance_type, state FROM Laptop WHERE state = 'AvailableState'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<Laptop> availableLaptops = new ArrayList<>();
            while (rs.next()) {
                Laptop laptop = mapResultSetToLaptop(rs);
                availableLaptops.add(laptop);
            }
            return availableLaptops;
        } catch (SQLException e) {
            handleSQLException("Fejl ved hentning af tilgængelige laptops", e);
            throw e;
        }
    }

    /**
     * Henter alle udlånte laptops fra databasen.
     *
     * @return Liste af udlånte laptops
     * @throws SQLException hvis der er problemer med databasen
     */
    public List<Laptop> getLoanedLaptops() throws SQLException {
        String sql = "SELECT laptop_uuid, brand, model, gigabyte, ram, performance_type, state FROM Laptop WHERE state = 'LoanedState'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<Laptop> loanedLaptops = new ArrayList<>();
            while (rs.next()) {
                Laptop laptop = mapResultSetToLaptop(rs);
                loanedLaptops.add(laptop);
            }
            return loanedLaptops;
        } catch (SQLException e) {
            handleSQLException("Fejl ved hentning af udlånte laptops", e);
            throw e;
        }
    }


    private Laptop mapResultSetToLaptop(ResultSet rs) throws SQLException {
        UUID laptopId = UUID.fromString(rs.getString("laptop_uuid"));
        String brand = rs.getString("brand");
        String model = rs.getString("model");
        int gigabyte = rs.getInt("gigabyte");
        int ram = rs.getInt("ram");
        String performanceTypeStr = rs.getString("performance_type");

        // Konverter strengværdien fra databasen til PerformanceTypeEnum
        PerformanceTypeEnum performanceType;
        try {
            performanceType = PerformanceTypeEnum.valueOf(performanceTypeStr);
        } catch (IllegalArgumentException e) {
            // Håndter tilfælde hvor strengen ikke matcher en enum-værdi
            logger.warning("Ukendt performance type i databasen: " + performanceTypeStr + ". Bruger LOW som standard.");
            performanceType = PerformanceTypeEnum.LOW; // Standard værdi hvis konvertering fejler
        }

        // Opret ny laptop med den konverterede enum-værdi
        Laptop laptop = new Laptop(laptopId, brand, model, gigabyte, ram, performanceType);

        // Sæt tilstanden baseret på databaseværdien
        String stateName = rs.getString("state");
        if (stateName != null) {
            laptop.setStateFromDatabase(stateName);
        }

        return laptop;
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