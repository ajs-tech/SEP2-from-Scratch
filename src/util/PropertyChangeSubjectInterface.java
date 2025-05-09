package util;

import java.beans.PropertyChangeListener;

public interface PropertyChangeSubjectInterface {
    void addListener(PropertyChangeListener listener);
    void removeListener(PropertyChangeListener listener);
    void addListener(String propertyName, PropertyChangeListener listener);
    void removeListener(String propertyName, PropertyChangeListener listener);
}
