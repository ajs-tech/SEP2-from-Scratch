package objects;

import java.io.Serializable;

/**
 * Interface for State Pattern applied to model.models.Laptop.
 * Defines behavior for different states a laptop can be in.
 */
public interface LaptopState extends Serializable {
    void click(Laptop laptop);
    String getSimpleName();
}