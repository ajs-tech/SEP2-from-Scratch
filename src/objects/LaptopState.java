package objects;

/**
 * Interface for State Pattern applied to model.models.Laptop.
 * Defines behavior for different states a laptop can be in.
 */
public interface LaptopState {

    void click(Laptop laptop);
    String getSimpleName();
}
