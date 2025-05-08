package view;

import core.ViewHandler;

public interface ViewController {
    void init(ViewHandler viewHandler);
    void close();
}
