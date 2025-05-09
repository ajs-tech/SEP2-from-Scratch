package view.controllers;

import core.ViewHandler;
import core.ViewmModelFactory;

public interface ViewController {
    void init(ViewHandler viewHandler, ViewmModelFactory viewmModelFactory);
    void close();
}
