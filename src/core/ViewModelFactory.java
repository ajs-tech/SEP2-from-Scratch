package viewmodel;

import model.ModelFactory;

/**
 * Factory som skaber og håndterer alle ViewModels.
 * Implementerer Factory Pattern for at centralisere oprettelsen af ViewModels.
 * Fungerer som bro mellem Views og Model laget.
 */
public class ViewModelFactory {

  private final ModelFactory modelFactory;

  // ViewModel instances (lazy initialization)
  private LaptopManagementMenuViewModel laptopManagementMenuViewModel;
  private AvailableLaptopsViewModel availableLaptopsViewModel;
  private StudentLaptopViewModel studentLaptopViewModel;
  private LoanOverviewViewModel loanOverviewViewModel;
  private ReturnComputerViewModel returnComputerViewModel;

  /**
   * Konstruktør for ViewModelFactory.
   *
   * @param modelFactory Factory som giver adgang til model objekter
   */
  public ViewModelFactory(ModelFactory modelFactory) {
    this.modelFactory = modelFactory;
  }

  /**
   * Returnerer ViewModel for hovedmenuen.
   * Lazy instantiering - opretter kun én instans når der er behov for det.
   *
   * @return LaptopManagementMenuViewModel instans
   */
  public LaptopManagementMenuViewModel getLaptopManagementMenuViewModel() {
    if (laptopManagementMenuViewModel == null) {
      laptopManagementMenuViewModel = new LaptopManagementMenuViewModel(modelFactory.getDataModel());
    }
    return laptopManagementMenuViewModel;
  }

  /**
   * Returnerer ViewModel for visning af tilgængelige laptops.
   *
   * @return AvailableLaptopsViewModel instans
   */
  public AvailableLaptopsViewModel getAvailableLaptopsViewModel() {
    if (availableLaptopsViewModel == null) {
      availableLaptopsViewModel = new AvailableLaptopsViewModel(modelFactory.getDataModel());
    }
    return availableLaptopsViewModel;
  }

  /**
   * Returnerer ViewModel for student laptop tildeling.
   *
   * @return StudentLaptopViewModel instans
   */
  public StudentLaptopViewModel getStudentLaptopViewModel() {
    if (studentLaptopViewModel == null) {
      studentLaptopViewModel = new StudentLaptopViewModel(modelFactory.getDataModel());
    }
    return studentLaptopViewModel;
  }

  /**
   * Returnerer ViewModel for låneoversigt.
   *
   * @return LoanOverviewViewModel instans
   */
  public LoanOverviewViewModel getLoanOverviewViewModel() {
    if (loanOverviewViewModel == null) {
      loanOverviewViewModel = new LoanOverviewViewModel(modelFactory.getDataModel());
    }
    return loanOverviewViewModel;
  }

  /**
   * Returnerer ViewModel for laptop returnering.
   *
   * @return ReturnComputerViewModel instans
   */
  public ReturnComputerViewModel getReturnComputerViewModel() {
    if (returnComputerViewModel == null) {
      returnComputerViewModel = new ReturnComputerViewModel(modelFactory.getDataModel());
    }
    return returnComputerViewModel;
  }
}