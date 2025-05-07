package view;

import core.ViewHandler;

/**
 * Interface for all view controllers to implement
 */
public interface ViewController {
    /**
     * Initializes the controller with the view handler and view model
     * @param viewHandler The view handler for navigation
     * @param viewModel The view model for this view
     */
    void init(ViewHandler viewHandler, Object viewModel);
}