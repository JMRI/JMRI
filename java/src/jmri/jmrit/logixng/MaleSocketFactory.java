package jmri.jmrit.logixng;

import jmri.NamedBean;

/**
 * Factory class for male sockets. This class is used when a tool wants to add
 * extra male sockets around the male sockets for actions and expressions. An
 * example is the debugger.
 * 
 * @author Daniel Bergqvist Copyright 2020
 * @param <T> the type of male socket
 */
public interface MaleSocketFactory<T extends NamedBean> {
    
    /**
     * Encapsulate a male socket into another male socket.
     * @param manager the manager that registers this male socket
     * @param maleSocket the male socket to be encapsulated
     * @return the new male socket that encapsulates the old male socket
     */
    public T encapsulateMaleSocket(BaseManager<T> manager, T maleSocket);
    
}
