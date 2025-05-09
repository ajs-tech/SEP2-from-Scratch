package view.Controller;

import core.ViewHandler;
import viewmodel.ViewModelFactory;

/**
 * Fælles interface for alle controllere i applikationen.
 * Definerer standardmetoder som alle controllere skal implementere.
 */
public interface Controller {

    /**
     * Initialiserer controlleren efter FXML loader har injected UI elementerne.
     * @param viewHandler Reference til ViewHandler for navigation mellem views
     * @param viewModelFactory Factory som giver adgang til ViewModels
     */
    void init(ViewHandler viewHandler, ViewModelFactory viewModelFactory);

    /**
     * Kaldes når viewet vises, for at opdatere UI-elementer eller
     * reinitialisere data efter behov.
     */
    void reset();
}