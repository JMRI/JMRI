package jmri;

/**
 * Interface for obtaining AnalogIOs.
 * 
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public interface AnalogIOManager extends Manager<AnalogIO> {

    /**
     * Add a type of NamedBean, for example VariableLight, that should be also registred in AnalogIOManager.
     * @param clazz the NamedBean class that should be registered in this manager
     * @param manager the manager that managers the NamedBeans of type clazz
     */
    public default void addBeanType(Class<? extends AnalogIO> clazz, Manager<? extends NamedBean> manager) {
        throw new UnsupportedOperationException();
    }

    /**
     * Remove a type of NamedBean, for example VariableLight, from beeing registred in AnalogIOManager.
     * @param clazz the NamedBean class that should be registered in this manager
     * @param manager the manager that managers the NamedBeans of type clazz
     */
    public default void removeBeanType(Class<? extends AnalogIO> clazz, Manager<? extends NamedBean> manager) {
        throw new UnsupportedOperationException();
    }

}
