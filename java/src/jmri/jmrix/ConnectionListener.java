package jmri.jmrix;

/**
 * A connection listener listens on a connection for when the connection
 * connects or disconnects.
 *
 * @author Daniel Bergqvist (C) 2026
 */
public interface ConnectionListener {

    /**
     * The connection has connected.
     *
     * @param adapter the adapter for the connection
     */
    void connected(PortAdapter adapter);

    /**
     * The connection has disconnected.
     *
     * @param adapter the adapter for the connection
     */
    void disconnected(PortAdapter adapter);

}
