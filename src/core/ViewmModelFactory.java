package core;

import model.Model;
import model.ModelImpl;
import viewmodel.*;

public class ViewmModelFactory {
    private static ViewmModelFactory INSTANCE;
    private Model model;

    private AvailableLaptopsViewModel availableLaptopsViewModel;
    private CreateStudentViewModel createStudentViewModel;
    private LaptopManagementMenuViewModel laptopManagementMenuViewModel;
    private LoanOverviewViewModel loanOverviewViewModel;
    private ReturnLaptopViewModel returnLaptopViewModel;

    private ViewmModelFactory(){
        model = ModelImpl.getInstance();
        availableLaptopsViewModel = new AvailableLaptopsViewModel(model);
        createStudentViewModel = new CreateStudentViewModel(model);
        laptopManagementMenuViewModel = new LaptopManagementMenuViewModel(model);
        loanOverviewViewModel = new LoanOverviewViewModel(model);
        returnLaptopViewModel = new ReturnLaptopViewModel(model);
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
        return model;
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
