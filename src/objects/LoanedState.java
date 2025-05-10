package objects;


public class LoanedState implements LaptopState {
    public final static String simpleName = "LoanedState";

    @Override
    public void click(Laptop laptop) {
        laptop.changeState(new AvailableState());
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