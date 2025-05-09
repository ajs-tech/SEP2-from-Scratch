package view.Controller;

import view.ViewHandler;
import core.ViewModelFactory;

/**
 * Interface som alle controllers skal implementere.
 * Definerer standardmetoder for initialisering og nulstilling.
 */
public interface Controller {

    /**
     * Initialiserer controlleren med nødvendige referencer.
     *
     * @param viewHandler Reference til ViewHandler for navigering mellem views
     * @param viewModelFactory Factory som giver adgang til ViewModels
     */
    void init(ViewHandler viewHandler, ViewModelFactory viewModelFactory);

    /**
     * Genindlæser viewet med opdaterede data.
     * Kaldes typisk når viewet vises igen efter at have været skjult.
     */
    void reset();
}