package jmri.jmris;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import jmri.InstanceManager;
import jmri.jmris.json.JsonServerPreferences;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
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

    /**
     * @deprecated see {@link #getSession() }
     * @return the WebSocket session
     */
    @Deprecated
    public Session getWebSocketConnection() {
        return this.getSession();
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
        if (this.dataOutputStream != null) {
            this.dataOutputStream.writeBytes(message);
        } else if (this.session != null && this.session.isOpen()) {
            Future<Void> future = null;
            try {
                future = this.session.getRemote().sendStringByFuture(message);
                future.get(InstanceManager.getDefault(JsonServerPreferences.class).getHeartbeatInterval(), TimeUnit.MILLISECONDS);
            } catch (WebSocketException ex) {
                log.debug("Exception sending message", ex);
                // A WebSocketException is most likely a broken socket,
                // so rethrow it as an IOException
                throw new IOException("Unable to send message", ex);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                // include only first 75 characters of message in log unless debugging
                String logMessage = (log.isDebugEnabled() || message.length() <= 75)
                        ? message
                        : message.substring(0, 75 - 1);
                log.error("Exception \"{}\" sending {}", ex.getMessage(), logMessage, ex);
                if (future != null) {
                    future.cancel(true);
                }
                JmriConnection.this.getSession().close(StatusCode.NO_CODE, ex.getMessage());
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
