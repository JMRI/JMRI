package jmri.util;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Common utility methods for working with Sockets.
 * <P>
 * We needed a place to refactor common socket-handling idioms in JMRI code, so
 * this class was created. It's more of a library of procedures than a real
 * class, as (so far) all of the operations have needed no state information.
 * <P>
 * In some cases, these routines use a Java 1.3 or later method, falling back to
 * an explicit implementation when running on Java 1.1
 *
 * @author Bob Jacobsen Copyright 2006
 * @deprecated since 4.7.1; use methods of {@link java.net.Socket} or
 * {@link java.net.ServerSocket} directly
 */
@Deprecated
public class SocketUtil {

    /**
     * Return the remote address, if possible, otherwise {@literal <unknown>}
     *
     * @param socket the remotely connected socket
     * @return the address or {@literal <unknown>}
     * @deprecated since 4.7.1; use
     * {@link java.net.Socket#getRemoteSocketAddress()} instead
     */
    @Deprecated
    static public String getRemoteSocketAddress(Socket socket) {
        return socket.getRemoteSocketAddress().toString();
    }

    /**
     * Set the Socket's reuseAddress parameter while protecting against failure
     *
     * @param socket the socket to set
     * @param on     true if the address should be reused; false otherwise
     * @see java.net.ServerSocket#setReuseAddress(boolean)
     * @deprecated since 4.7.1; use
     * {@link java.net.ServerSocket#setReuseAddress(boolean)} instead
     */
    @Deprecated
    static public void setReuseAddress(ServerSocket socket, boolean on) {
        try {
            socket.setReuseAddress(on);
        } catch (SocketException e) {
            // ignore any errors
        }
    }

}
