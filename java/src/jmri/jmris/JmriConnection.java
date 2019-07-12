package jmri.jmris;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstraction of DataOutputStream and WebSocket.Connection classes.
 * <p>
 * Used so that that server objects need only to use a single object/method to
 * send data to any supported object type.
 *
 * @author rhwood Randall Wood Copyright (C) 2012, 2014
 */
public class JmriConnection {

    private final Session session;
    private final DataOutputStream dataOutputStream;
    private Locale locale = Locale.getDefault();
    private final static Logger log = LoggerFactory.getLogger(JmriConnection.class);

    /**
     * Create a JmriConnection that sends output to a WebSocket.
     *
     * @param connection WebSocket Session to use.
     */
    public JmriConnection(Session connection) {
        this.session = connection;
        this.dataOutputStream = null;
    }

    /**
     * Create a JmriConnection that sends output to a DataOutputStream.
     *
     * @param output DataOutputStream to use
     */
    public JmriConnection(DataOutputStream output) {
        this.dataOutputStream = output;
        this.session = null;
    }

    /**
     * Get the WebSocket session.
     *
     * @return the WebSocket session
     */
    public Session getSession() {
        return this.session;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    /**
     * Send a String to the instantiated connection.
     * <p>
     * This method throws an IOException so the server or servlet holding the
     * connection open can respond to the exception if there is an immediate
     * failure. If there is an asynchronous failure, the connection is closed.
     *
     * @param message message to send
     * @throws IOException if problem sending message
     */
    public void sendMessage(String message) throws IOException {
        log.trace("Sending \"{}\"", message);
        if (this.dataOutputStream != null) {
            this.dataOutputStream.writeBytes(message);
        } else if (this.session != null) {
            if (this.session.isOpen()) {
                try {
                    RemoteEndpoint remote = this.session.getRemote();
                    // The JSON sockets keep an internal state variable and throw an IllegalStateException if more than one thread attempts to do sendString at the same time. This function gets normally called from a mixture of the Layout thread and the WebServer-NN threads.
                    synchronized(remote) {
                        remote.sendString(message);
                    }
                } catch (WebSocketException ex) {
                    log.debug("Exception sending message", ex);
                    // A WebSocketException is most likely a broken socket,
                    // so rethrow it as an IOException
                    if (ex.getMessage() == null) {
                        // provide a generic message if ex has no message
                        throw new IOException("Exception sending message", ex);
                    }
                    throw new IOException(ex);
                } catch (IOException ex) {
                    if (ex.getMessage() == null) {
                        // provide a generic message if ex has no message
                        throw new IOException("Exception sending message", ex);
                    }
                    throw ex; // rethrow if complete
                }
            } else {
                // immediately thrown an IOException to trigger closing
                // actions up the call chain
                throw new IOException("Will not send message on non-open session");
            }
        }
    }

    /**
     * Close the connection.
     * <p>
     * Note: Objects using JmriConnection with a
     * {@link org.eclipse.jetty.websocket.api.Session} may prefer to use
     * <code>getSession().close()</code> since Session.close() does not throw an
     * IOException.
     *
     * @throws IOException if problem closing connection
     */
    public void close() throws IOException {
        if (this.dataOutputStream != null) {
            this.dataOutputStream.close();
        } else if (this.session != null) {
            this.session.close();
        }
    }

    /**
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
