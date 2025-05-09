package core;

import model.Model;
import model.ModelImpl;
import viewmodel.*;

public class ViewmModelFactory {
    private static ViewmModelFactory INSTANCE;
    private Model laptopRentModel;
    private AvailableLaptopsViewModel availableLaptopsViewModel;
    private CreateStudentViewModel createStudentViewModel;
    private LaptopManagementMenuViewModel laptopManagementMenuViewModel;
    private LoanOverviewViewModel loanOverviewViewModel;
    private ReturnLaptopViewModel returnLaptopViewModel;

    private ViewmModelFactory(){
        laptopRentModel = new ModelImpl();
        availableLaptopsViewModel = new AvailableLaptopsViewModel(laptopRentModel);
        createStudentViewModel = new CreateStudentViewModel(laptopRentModel);
        laptopManagementMenuViewModel = new LaptopManagementMenuViewModel(laptopRentModel);
        loanOverviewViewModel = new LoanOverviewViewModel(laptopRentModel);
        returnLaptopViewModel = new ReturnLaptopViewModel(laptopRentModel);
    }

    public static ViewmModelFactory getInstance(){
        if (INSTANCE == null){
            synchronized (ViewmModelFactory.class){
                if (INSTANCE == null){
                    INSTANCE = new ViewmModelFactory();
                    return INSTANCE;
                }
            }
        }
        return INSTANCE;
    }

    public Model getLaptopRentModel(){
        return laptopRentModel;
    }

    public AvailableLaptopsViewModel getAvailableLaptopsViewModel(){
        return availableLaptopsViewModel;
    }

    public CreateStudentViewModel getCreateStudentViewModel(){
        return createStudentViewModel;
    }

    public LaptopManagementMenuViewModel getLaptopManagementMenuViewModel(){
        return laptopManagementMenuViewModel;
    }

    public LoanOverviewViewModel getLoanOverviewViewModel(){
        return loanOverviewViewModel;
    }

    public ReturnLaptopViewModel getReturnLaptopViewModel(){
        return returnLaptopViewModel;
    }


}
