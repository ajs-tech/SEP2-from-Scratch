package viewmodel;

import enums.PerformanceTypeEnum;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import model.ModelImpl;
import objects.Laptop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class CreateLaptopViewModel implements PropertyChangeListener {
    private Model model;

    private StringProperty brandProperty;
    private StringProperty modelProperty;
    private StringProperty ramProperty;
    private StringProperty diskProperty;
    private BooleanProperty highPerformanceProperty;

    private StringProperty resultLaptopProperty;
    private StringProperty resultPerformanceTypeProperty;
    private StringProperty resultSpecsProperty;
    private StringProperty resultStatusProperty;
    private StringProperty errorProperty;
    private StringProperty statusProperty;

    private IntegerProperty totalLaptopsProperty;
    private IntegerProperty availableLaptopsCountProperty;
    private IntegerProperty loanedLaptopsCountProperty;

    private ObservableList<LaptopTableItem> allLaptops;
    private ObservableList<LaptopTableItem> availableLaptops;
    private ObservableList<LaptopTableItem> loanedLaptops;

    private Laptop lastCreatedLaptop;

    public CreateLaptopViewModel(Model model) {
        this.model = model;

        brandProperty = new SimpleStringProperty("");
        modelProperty = new SimpleStringProperty("");
        ramProperty = new SimpleStringProperty("");
        diskProperty = new SimpleStringProperty("");
        highPerformanceProperty = new SimpleBooleanProperty(false);

        resultLaptopProperty = new SimpleStringProperty("Ingen handling endnu");
        resultPerformanceTypeProperty = new SimpleStringProperty("");
        resultSpecsProperty = new SimpleStringProperty("");
        resultStatusProperty = new SimpleStringProperty("");
        errorProperty = new SimpleStringProperty("");
        statusProperty = new SimpleStringProperty("Klar til at oprette computer");

        totalLaptopsProperty = new SimpleIntegerProperty(0);
        availableLaptopsCountProperty = new SimpleIntegerProperty(0);
        loanedLaptopsCountProperty = new SimpleIntegerProperty(0);

        allLaptops = FXCollections.observableArrayList();
        availableLaptops = FXCollections.observableArrayList();
        loanedLaptops = FXCollections.observableArrayList();

        model.addListener(this);
    }

    public boolean createLaptop() {
        try {
            errorProperty.set("");

            if (brandProperty.get() == null || brandProperty.get().trim().isEmpty()) {
                errorProperty.set("Mærke skal udfyldes");
                return false;
            }

            if (modelProperty.get() == null || modelProperty.get().trim().isEmpty()) {
                errorProperty.set("Model skal udfyldes");
                return false;
            }

            if (ramProperty.get() == null || ramProperty.get().trim().isEmpty()) {
                errorProperty.set("RAM skal angives");
                return false;
            }

            if (diskProperty.get() == null || diskProperty.get().trim().isEmpty()) {
                errorProperty.set("Diskstørrelse skal angives");
                return false;
            }

            int ram;
            int disk;

            try {
                ram = Integer.parseInt(ramProperty.get().trim());
                if (ram <= 0 || ram > 128) {
                    errorProperty.set("RAM skal være mellem 1 og 128 GB");
                    return false;
                }
            } catch (NumberFormatException e) {
                errorProperty.set("RAM skal være et heltal");
                return false;
            }

            try {
                disk = Integer.parseInt(diskProperty.get().trim());
                if (disk <= 0 || disk > 4000) {
                    errorProperty.set("Diskstørrelse skal være mellem 1 og 4000 GB");
                    return false;
                }
            } catch (NumberFormatException e) {
                errorProperty.set("Diskstørrelse skal være et heltal");
                return false;
            }

            PerformanceTypeEnum performanceType = highPerformanceProperty.get() ?
                    PerformanceTypeEnum.HIGH : PerformanceTypeEnum.LOW;

            Laptop laptop = model.createLaptop(
                    brandProperty.get().trim(),
                    modelProperty.get().trim(),
                    disk,
                    ram,
                    performanceType
            );

            if (laptop == null) {
                errorProperty.set("Fejl ved oprettelse af computer");
                return false;
            }

            lastCreatedLaptop = laptop;
            updateResultPanel(laptop);
            statusProperty.set("Computer oprettet");
            clearForm();

            return true;
        } catch (Exception e) {
            errorProperty.set("Fejl: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void updateResultPanel(Laptop laptop) {
        if (laptop != null) {
            resultLaptopProperty.set(laptop.getBrand() + " " + laptop.getModel() + " (ID: " + laptop.getId() + ")");

            String performanceText = laptop.getPerformanceType() == PerformanceTypeEnum.HIGH ?
                    "Høj (Udvikling, design)" : "Lav (Office, internet)";
            resultPerformanceTypeProperty.set(performanceText);

            resultSpecsProperty.set(laptop.getRam() + " GB RAM, " + laptop.getGigabyte() + " GB Disk");
            resultStatusProperty.set("Computer oprettet og er nu tilgængelig");
        }
    }

    public void clearForm() {
        brandProperty.set("");
        modelProperty.set("");
        ramProperty.set("");
        diskProperty.set("");
        highPerformanceProperty.set(false);
        errorProperty.set("");
    }

    public void refreshLaptops() {
        try {
            List<Laptop> all = model.getAllLaptops();
            List<Laptop> available = model.getAvailableLaptops();
            List<Laptop> loaned = model.getLoanedLaptops();

            totalLaptopsProperty.set(all != null ? all.size() : 0);
            availableLaptopsCountProperty.set(available != null ? available.size() : 0);
            loanedLaptopsCountProperty.set(loaned != null ? loaned.size() : 0);

            updateLaptopTableItems(all, allLaptops);
            updateLaptopTableItems(available, availableLaptops);
            updateLaptopTableItems(loaned, loanedLaptops);

            statusProperty.set("Lister opdateret");
        } catch (Exception e) {
            statusProperty.set("Fejl ved opdatering af lister: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateLaptopTableItems(List<Laptop> laptops, ObservableList<LaptopTableItem> items) {
        items.clear();
        if (laptops != null) {
            for (Laptop laptop : laptops) {
                items.add(new LaptopTableItem(laptop));
            }
        }
    }

    public StringProperty brandProperty() {
        return brandProperty;
    }

    public StringProperty modelProperty() {
        return modelProperty;
    }

    public StringProperty ramProperty() {
        return ramProperty;
    }

    public StringProperty diskProperty() {
        return diskProperty;
    }

    public BooleanProperty highPerformanceProperty() {
        return highPerformanceProperty;
    }

    public StringProperty resultLaptopProperty() {
        return resultLaptopProperty;
    }

    public StringProperty resultPerformanceTypeProperty() {
        return resultPerformanceTypeProperty;
    }

    public StringProperty resultSpecsProperty() {
        return resultSpecsProperty;
    }

    public StringProperty resultStatusProperty() {
        return resultStatusProperty;
    }

    public StringProperty errorProperty() {
        return errorProperty;
    }

    public StringProperty statusProperty() {
        return statusProperty;
    }

    public IntegerProperty totalLaptopsProperty() {
        return totalLaptopsProperty;
    }

    public IntegerProperty availableLaptopsCountProperty() {
        return availableLaptopsCountProperty;
    }

    public IntegerProperty loanedLaptopsCountProperty() {
        return loanedLaptopsCountProperty;
    }

    public ObservableList<LaptopTableItem> getAllLaptops() {
        return allLaptops;
    }

    public ObservableList<LaptopTableItem> getAvailableLaptops() {
        return availableLaptops;
    }

    public ObservableList<LaptopTableItem> getLoanedLaptops() {
        return loanedLaptops;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();

        if (propertyName.equals(ModelImpl.EVENT_LAPTOP_CREATED) ||
                propertyName.equals(ModelImpl.EVENT_LAPTOP_DELETED) ||
                propertyName.equals(ModelImpl.EVENT_LAPTOP_STATE_CHANGED) ||
                propertyName.equals(ModelImpl.EVENT_LAPTOP_UPDATED) ||
                propertyName.equals(ModelImpl.EVENT_AVAILABLE_COUNT_CHANGED) ||
                propertyName.equals(ModelImpl.EVENT_LOANED_COUNT_CHANGED)) {

            refreshLaptops();
        }
    }

    public static class LaptopTableItem {
        private String id;
        private String brand;
        private String model;
        private int ram;
        private int disk;
        private String performanceType;
        private String status;

        public LaptopTableItem(Laptop laptop) {
            this.id = laptop.getId().toString();
            this.brand = laptop.getBrand();
            this.model = laptop.getModel();
            this.ram = laptop.getRam();
            this.disk = laptop.getGigabyte();
            this.performanceType = laptop.getPerformanceType().toString();
            this.status = laptop.isAvailable() ? "Tilgængelig" : "Udlånt";
        }

        public String getId() {
            return id;
        }

        public String getBrand() {
            return brand;
        }

        public String getModel() {
            return model;
        }

        public int getRam() {
            return ram;
        }

        public int getDisk() {
            return disk;
        }

        public String getPerformanceType() {
            return performanceType;
        }

        public String getStatus() {
            return status;
        }
    }
}