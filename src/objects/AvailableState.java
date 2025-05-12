package objects;

/**
 * Concrete implementation of LaptopState for laptops that are available.
 * Part of State Pattern.
 */
public class AvailableState implements LaptopState {
    private static final long serialVersionUID = 4L;
    public final static String simpleName = "AvailableState";

    @Override
    public void click(Laptop laptop) {
        laptop.changeState(new LoanedState());
    }

    @Override
    public String getSimpleName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getSimpleName();
    }
}
