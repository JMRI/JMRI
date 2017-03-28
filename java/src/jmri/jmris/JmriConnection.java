package jmri.jmris;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.WriteCallback;
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
     * @param connection WebSocket Session to use.
     */
    public JmriConnection(Session connection) {
        this.session = connection;
    }

    /**
     * Create a JmriConnection that sends output to a DataOutputStream.
     *
     * @param output DataOutputStream to use
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
     * connection open can respond to the exception if there is an immediate
     * failure. If there is an asynchronous failure, the connection is closed.
     *
     * @param message message to send
     * @throws IOException if problem sending message
     */
    public void sendMessage(String message) throws IOException {
        if (this.dataOutputStream != null) {
            this.dataOutputStream.writeBytes(message);
        } else if (this.session != null && this.session.isOpen()) {
            try {
                this.session.getRemote().sendString(message, new WriteCallback() {
                    @Override
                    public void writeFailed(Throwable thrwbl) {
                        if (log.isDebugEnabled()) {
                            // include entire message in log
                            log.error("Exception \"{}\" sending {}", thrwbl.getMessage(), message, thrwbl);
                        } else {
                            // include only first 75 characters of message in log
                            int length = 75;
                            log.error("Exception \"{}\" sending {}", thrwbl,
                                    (message.length() > length)
                                    ? message.substring(0, length - 1)
                                    : message);
                        }
                        JmriConnection.this.getSession().close(StatusCode.NO_CODE, thrwbl.getMessage());
                    }

                    @Override
                    public void writeSuccess() {
                        log.debug("Sent {}", message);
                    }
                });
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
