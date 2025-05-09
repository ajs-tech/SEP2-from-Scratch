package core;

import model.logic.DataManager;
import model.logic.DataModel;

/**
 * Factory som skaber og håndterer alle model objekter.
 * Implementerer Factory Pattern for at centralisere oprettelsen af model lag.
 */
public class ModelFactory {

  private DataModel dataModel;

  /**
   * Konstruktør for ModelFactory.
   * Initialiserer ingen komponenter ved oprettelse for lazy loading.
   */
  public ModelFactory() {
    // Tom konstruktør - objekter oprettes først ved behov
  }

  /**
   * Returnerer DataModel instansen.
   * Lazy instantiering - opretter kun én instans når der er behov for det.
   *
   * @return DataModel instans
   */
  public DataModel getDataModel() {
    if (dataModel == null) {
      dataModel = new DataManager();
    }
    return dataModel;
  }
}