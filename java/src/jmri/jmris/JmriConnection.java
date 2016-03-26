package jmri.jmris;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstraction of DataOutputStream and WebSocket.Connection classes.
 *
 * Used so that that server objects need only to use a single object/method to
 * send data to any supported object type.
 *
 * @author rhwood Randall Wood Copyright (C) 2012, 2014
 */
public class JmriConnection {

    private Session session = null;
    private DataOutputStream dataOutputStream = null;
    private Locale locale = Locale.getDefault();
    private final static Logger log = LoggerFactory.getLogger(JmriConnection.class);

    /**
     * Create a JmriConnection that sends output to a WebSocket.
     *
     * @param connection
     */
    public JmriConnection(Session connection) {
        this.session = connection;
    }

    /**
     * Create a JmriConnection that sends output to a DataOutputStream.
     *
     * @param output
     */
    public JmriConnection(DataOutputStream output) {
        this.dataOutputStream = output;
    }

    /**
     * Get the WebSocket session.
     *
     * @return the WebSocket session
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * @deprecated see {@link #getSession() }
     * @return the WebSocket session
     */
    @Deprecated
    public Session getWebSocketConnection() {
        return this.getSession();
    }

    /**
     * Set the WebSocket session.
     *
     * @param session the WebSocket session
     */
    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * @deprecated see {@link #setSession(org.eclipse.jetty.websocket.api.Session)
     * }
     *
     * @param webSocketConnection the WebSocket session
     */
    @Deprecated
    public void setWebSocketConnection(Session webSocketConnection) {
        this.setSession(session);
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void setDataOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    /**
     * Send a String to the instantiated connection.
     *
     * This method throws an IOException so the server or servlet holding the
     * connection open can respond to the exception.
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        log.debug("Sending {}", message);
        if (this.dataOutputStream != null) {
            this.dataOutputStream.writeBytes(message);
        } else if (this.session != null && this.session.isOpen()) {
            try {
                this.session.getRemote().sendStringByFuture(message);
            } catch (WebSocketException ex) {
                log.debug("Exception sending message", ex);
                // A WebSocketException is most likely a broken socket,
                // so rethrow it as an IOException
                throw new IOException(ex);
            }
        }
    }

    /**
     * Close the connection.
     *
     * Note: Objects using JmriConnection with a
     * {@link org.eclipse.jetty.websocket.api.Session} may prefer to use
     * <code>getSession().close()</code> since Session.close() does not throw an
     * IOException.
     *
     * @throws IOException
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
