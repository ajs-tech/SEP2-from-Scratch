package core;

import model.Model;
import model.ModelImpl;

public class ModelFactory {
    private static ModelFactory INSTANCE;
    private Model modelImp;

    private ModelFactory(){
        modelImp = ModelImpl.getInstance();
    }

    public static ModelFactory getInstance(){
        if (INSTANCE == null){
            synchronized (ModelFactory.class){
                if (INSTANCE == null){
                    INSTANCE = new ModelFactory();
                    return INSTANCE;
                }
            }
        }
        return INSTANCE;
    }

    public Model getModelImp(){
        return modelImp;
    }
}
