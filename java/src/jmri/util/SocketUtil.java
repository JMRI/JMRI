// SocketUtil.java
package jmri.util;

import java.net.ServerSocket;
import java.net.Socket;

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
 * @version $Revision$
 */
public class SocketUtil {

    /**
     * Return the remote address, if possible, otherwise {@code "<unknown>"}
     */
    static public String getRemoteSocketAddress(Socket socket) {
        try {
            return socket.getRemoteSocketAddress().toString();
        } catch (Throwable e) {
        } finally {
//            return "<unknown>";
        }
        return "<unknown>";

    }

    /**
     * Set the Socket's reuseAddress parameter while protecting against failure
     */
    static public void setReuseAddress(ServerSocket socket, boolean on) {
        try {
            socket.setReuseAddress(on);
        } catch (Throwable e) {
        }
    }

}
