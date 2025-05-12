package objects;

import enums.ReservationStatusEnum;
import util.PropertyChangeSubjectInterface;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;


public class Reservation implements PropertyChangeSubjectInterface, Serializable {
    private static final long serialVersionUID = 3L;

    private final UUID reservationId;
    private final Student student;
    private final Laptop laptop;
    private ReservationStatusEnum status;
    private final Date creationDate;
    private transient PropertyChangeSupport support;

    public static final String EVENT_STATUS_CHANGED = "reservation_status_changed";
    public static final String EVENT_COMPLETED = "reservation_completed";
    public static final String EVENT_CANCELLED = "reservation_cancelled";


    public Reservation(Student student, Laptop laptop) {
        this(UUID.randomUUID(), student, laptop, ReservationStatusEnum.ACTIVE, new Date());
    }

    public Reservation(UUID reservationId, Student student, Laptop laptop,
                       ReservationStatusEnum status, Date creationDate) {
        validateInput(student, laptop, status, creationDate);

        this.reservationId = reservationId;
        this.student = student;
        this.laptop = laptop;
        this.status = status;
        this.creationDate = creationDate;
        this.support = new PropertyChangeSupport(this);
    }


    public Reservation(UUID reservationId, Student student, Laptop laptop, ReservationStatusEnum status) {
        this(reservationId, student, laptop, status, new Date());
    }


    private void validateInput(Student student, Laptop laptop, ReservationStatusEnum status, Date creationDate) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null");
        }

        if (laptop == null) {
            throw new IllegalArgumentException("Laptop cannot be null");
        }

        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        if (creationDate == null) {
            throw new IllegalArgumentException("Creation date cannot be null");
        }
    }

    // Getters

    public UUID getReservationId() {
        return reservationId;
    }

    public Student getStudent() {
        return student;
    }

    public String getStudentDetailsString() {
        return student.toString();
    }

    public Laptop getLaptop() {
        return laptop;
    }

    public String getLaptopDetailsString() {
        return laptop.toString();
    }

    public ReservationStatusEnum getStatus() {
        return status;
    }

    public Date getCreationDate() {
        return creationDate;
    }


    public void changeStatus(ReservationStatusEnum newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }

        ReservationStatusEnum oldStatus = this.status;
        this.status = newStatus;

        // Fire basic status change event
        if (support != null) {
            support.firePropertyChange(EVENT_STATUS_CHANGED, oldStatus, newStatus);

            // Additional events for specific status changes
            if (oldStatus == ReservationStatusEnum.ACTIVE) {
                if (newStatus == ReservationStatusEnum.COMPLETED) {
                    // Make laptop available again
                    if (laptop.isLoaned()) {
                        laptop.changeState(new AvailableState());
                    }
                    support.firePropertyChange(EVENT_COMPLETED, oldStatus, newStatus);
                }
                else if (newStatus == ReservationStatusEnum.CANCELLED) {
                    // Make laptop available again
                    if (laptop.isLoaned()) {
                        laptop.changeState(new AvailableState());
                    }
                    support.firePropertyChange(EVENT_CANCELLED, oldStatus, newStatus);
                }
            }
        }
    }

    // This method is called during deserialization
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Initialize transient fields
        this.support = new PropertyChangeSupport(this);
    }

    @Override
    public String toString() {
        return "Reservation: " + student.getName() + " - " +
                laptop.getModel() + " (" + status + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(reservationId, that.reservationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }

    // Observer methods (added listeners)

    @Override
    public void addListener(PropertyChangeListener listener) {
        if (support == null) {
            support = new PropertyChangeSupport(this);
        }
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        if (support != null) {
            support.removePropertyChangeListener(listener);
        }
    }

    @Override
    public void addListener(String propertyName, PropertyChangeListener listener) {
        if (support == null) {
            support = new PropertyChangeSupport(this);
        }
        support.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removeListener(String propertyName, PropertyChangeListener listener) {
        if (support != null) {
            support.removePropertyChangeListener(propertyName, listener);
        }
    }
}