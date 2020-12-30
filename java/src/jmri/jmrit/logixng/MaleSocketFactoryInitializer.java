package jmri.jmrit.logixng;

import javax.annotation.Nonnull;

import jmri.NamedBean;

/**
 * Factory class for male sockets. This class is used when a tool wants to add
 * extra male sockets around the male sockets for actions and expressions. An
 * example is the debugger.
 * 
 * @author Daniel Bergqvist Copyright 2020
 * @param <T> the type of male socket
 */
public interface MaleSocketFactoryInitializer<T extends NamedBean> {
    
    /**
     * Provide a MaleSocketFactory instance of the given class.
     *
     * @param <T>  the class to get the default for
     * @param type the class to get the default for
     * @return the newly created default for the given class
     * @throws IllegalArgumentException if creating an instance of type is not
     *                                  supported by this InstanceInitalizer
     */
    public MaleSocketFactory<T> getDefault(@Nonnull Class<T> type);
    
}
