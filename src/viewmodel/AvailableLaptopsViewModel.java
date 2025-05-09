package viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.enums.PerformanceTypeEnum;
import model.logic.DataModel;
import model.models.Laptop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * ViewModel for visning af tilgængelige laptops.
 * Håndterer data og logik for AvailableLaptopsView.
 */
public class AvailableLaptopsViewModel implements PropertyChangeListener {

  private final DataModel model;

  // Properties til binding med View
  private final IntegerProperty totalAvailableCount;
  private final IntegerProperty highPerformanceCount;
  private final IntegerProperty lowPerformanceCount;

  // ObservableLists til tabeller - bruger nu indre klasser i stedet for separate model-klasser
  private final ObservableList<LaptopData> highPerformanceLaptops;
  private final ObservableList<LaptopData> lowPerformanceLaptops;

  /**
   * Indre klasse til at indkapsle laptop data til visning.
   */
  public static class LaptopData {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty brand = new SimpleStringProperty();
    private final StringProperty model = new SimpleStringProperty();
    private final IntegerProperty ram = new SimpleIntegerProperty();
    private final IntegerProperty diskSize = new SimpleIntegerProperty();
    private final StringProperty performanceType = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty assignedTo = new SimpleStringProperty();

    public LaptopData(String id, String brand, String model, int ram, int diskSize,
                      String performanceType, String status, String assignedTo) {
      this.id.set(id);
      this.brand.set(brand);
      this.model.set(model);
      this.ram.set(ram);
      this.diskSize.set(diskSize);
      this.performanceType.set(performanceType);
      this.status.set(status);
      this.assignedTo.set(assignedTo);
    }

    // Getters til properties
    public StringProperty idProperty() { return id; }
    public StringProperty brandProperty() { return brand; }
    public StringProperty modelProperty() { return model; }
    public IntegerProperty ramProperty() { return ram; }
    public IntegerProperty diskSizeProperty() { return diskSize; }
    public StringProperty performanceTypeProperty() { return performanceType; }
    public StringProperty statusProperty() { return status; }
    public StringProperty assignedToProperty() { return assignedTo; }
  }

  /**
   * Konstruktør for AvailableLaptopsViewModel.
   *
   * @param model Reference til DataModel interfacet
   */
  public AvailableLaptopsViewModel(DataModel model) {
    this.model = model;

    // Initialiser properties
    totalAvailableCount = new SimpleIntegerProperty(0);
    highPerformanceCount = new SimpleIntegerProperty(0);
    lowPerformanceCount = new SimpleIntegerProperty(0);

    // Initialiser observableLists
    highPerformanceLaptops = FXCollections.observableArrayList();
    lowPerformanceLaptops = FXCollections.observableArrayList();

    // Registrer som listener på model for at modtage opdateringer
    model.addPropertyChangeListener(this);

    // Initialiser data
    refreshData();
  }

  /**
   * Opdaterer data i ViewModel fra modellen.
   */
  public void refreshData() {
    updateLaptopLists();
    updateCounts();
  }

  /**
   * Opdaterer laptop lister fra model data.
   */
  private void updateLaptopLists() {
    // Hent alle laptops fra model
    List<Laptop> allLaptops = model.getAllLaptops();

    // Ryd eksisterende lister
    highPerformanceLaptops.clear();
    lowPerformanceLaptops.clear();

    // Filtrer og konverter laptops til LaptopData objekter
    for (Laptop laptop : allLaptops) {
      if (laptop.isAvailable()) {
        LaptopData laptopData = convertToLaptopData(laptop);

        if (laptop.getPerformanceType() == PerformanceTypeEnum.HIGH) {
          highPerformanceLaptops.add(laptopData);
        } else {
          lowPerformanceLaptops.add(laptopData);
        }
      }
    }
  }

  /**
   * Opdaterer tællere baseret på filtrerede lister.
   */
  private void updateCounts() {
    highPerformanceCount.set(highPerformanceLaptops.size());
    lowPerformanceCount.set(lowPerformanceLaptops.size());
    totalAvailableCount.set(highPerformanceCount.get() + lowPerformanceCount.get());
  }

  /**
   * Konverterer en model Laptop til LaptopData.
   */
  private LaptopData convertToLaptopData(Laptop laptop) {
    String id = laptop.getId().toString();
    String brand = laptop.getBrand();
    String model = laptop.getModel();
    int ram = laptop.getRam();
    int diskSize = laptop.getGigabyte();
    String performanceType = laptop.getPerformanceType().toString();
    String status = laptop.getStateClassName();
    String assignedTo = ""; // Tilgængelige laptops er ikke tildelt

    return new LaptopData(id, brand, model, ram, diskSize, performanceType, status, assignedTo);
  }

  /**
   * Håndterer propertyChange events fra model.
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String propertyName = evt.getPropertyName();

    if ("laptopStateChanged".equals(propertyName) ||
            "laptopCreated".equals(propertyName) ||
            "laptopUpdated".equals(propertyName) ||
            "laptopRemoved".equals(propertyName) ||
            "availableLaptopCount".equals(propertyName)) {

      refreshData();
    }
  }

  /**
   * Afslutter applikationen.
   * Oprydning, frigørelse af ressourcer osv.
   */
  public void closeApplication() {
    // Fjern denne ViewModel som listener
    model.removePropertyChangeListener(this);

    // Andre handlinger der er nødvendige inden lukning
  }

  // Getter metoder til properties og observable lists

  public IntegerProperty totalAvailableCountProperty() {
    return totalAvailableCount;
  }

  public IntegerProperty highPerformanceCountProperty() {
    return highPerformanceCount;
  }

  public IntegerProperty lowPerformanceCountProperty() {
    return lowPerformanceCount;
  }

  public ObservableList<LaptopData> getHighPerformanceLaptops() {
    return highPerformanceLaptops;
  }

  public ObservableList<LaptopData> getLowPerformanceLaptops() {
    return lowPerformanceLaptops;
  }
}