package viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import alt.logic.DataModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * ViewModel for hovedmenuen i laptop udlånssystemet.
 * Implementerer Observer pattern for at lytte efter model-ændringer.
 */
public class LaptopManagementMenuViewModel implements PropertyChangeListener {

  private final DataModel model;

  // Properties til binding med View
  private final StringProperty systemStatus;
  private final StringProperty availableLaptopsText;
  private final StringProperty loanedLaptopsText;

  /**
   * Konstruktør for LaptopManagementMenuViewModel.
   *
   * @param model Reference til DataModel interfacet
   */
  public LaptopManagementMenuViewModel(DataModel model) {
    this.model = model;

    // Initialiser properties
    systemStatus = new SimpleStringProperty("Systemstatus: Online");
    availableLaptopsText = new SimpleStringProperty("Tilgængelige computere: 0");
    loanedLaptopsText = new SimpleStringProperty("Udlånte computere: 0");

    // Registrer som listener på model for at modtage opdateringer
    model.addPropertyChangeListener(this);

    // Initialiser data
    refreshData();
  }

  /**
   * Opdaterer data i ViewModel fra modellen.
   */
  public void refreshData() {
    int availableLaptops = model.getAmountOfAvailableLaptops();
    int loanedLaptops = model.getAmountOfLoanedLaptops();

    availableLaptopsText.set("Tilgængelige computere: " + availableLaptops);
    loanedLaptopsText.set("Udlånte computere: " + loanedLaptops);
  }

  /**
   * Håndterer propertyChange events fra model.
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String propertyName = evt.getPropertyName();

    if ("availableLaptopCount".equals(propertyName) ||
            "loanedLaptopCount".equals(propertyName) ||
            "laptopStateChanged".equals(propertyName)) {

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

  // Getter metoder til properties (bruges til binding)

  public StringProperty systemStatusProperty() {
    return systemStatus;
  }

  public StringProperty availableLaptopsTextProperty() {
    return availableLaptopsText;
  }

  public StringProperty loanedLaptopsTextProperty() {
    return loanedLaptopsText;
  }
}