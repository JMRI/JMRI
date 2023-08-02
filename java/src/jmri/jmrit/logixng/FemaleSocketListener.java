package jmri.jmrit.logixng;

/**
 * A listener for when a socket is connected or disconnected.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface FemaleSocketListener {

    /**
     * The socket is connected.
     * @param socket the socket
     */
    void connected(FemaleSocket socket);

    /**
     * The socket is disconnected.
     * @param socket the socket
     */
    void disconnected(FemaleSocket socket);

    /**
     * The socket name is changed.
     * @param socket the socket
     */
    default void socketNameChanged(FemaleSocket socket) {
        // Do nothing
    }

}
