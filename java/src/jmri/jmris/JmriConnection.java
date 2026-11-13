package jmri.jmris;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.util.ClientMessageSender;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstraction of DataOutputStream and WebSocket.Connection classes.
 * <p>
 * Used so that that server objects need only to use a single object/method to
 * send data to any supported object type.
 * <p>
 * Messages are sent through a {@link ClientMessageSender}, so a message sent
 * from the layout thread (Swing thread) does not wait for a client that is not
 * reading. If a client falls too far behind, or a message can't be sent, the
 * connection is closed.
 *
 * @author Randall Wood Copyright (C) 2012, 2014
 */
public class JmriConnection {

    private final Session session;
    private final DataOutputStream dataOutputStream;
    private final ClientMessageSender sender;
    private Locale locale = Locale.getDefault();
    private static final Logger log = LoggerFactory.getLogger(JmriConnection.class);
    private static final String EX_SENDING_MSG = "Exception sending message";

    /**
     * Create a JmriConnection that sends output to a WebSocket.
     *
     * @param connection WebSocket Session to use.
     */
    public JmriConnection(Session connection) {
        this.session = connection;
        this.dataOutputStream = null;
        this.sender = new ClientMessageSender(clientName(), this::write, this::closeAfterFailure);
    }

    /**
     * Create a JmriConnection that sends output to a DataOutputStream.
     *
     * @param output DataOutputStream to use
     */
    public JmriConnection(DataOutputStream output) {
        this.dataOutputStream = output;
        this.session = null;
        this.sender = new ClientMessageSender(clientName(), this::write, this::closeAfterFailure);
    }

    private String clientName() {
        if (session != null && session.getRemoteAddress() != null) {
            return "Client " + session.getRemoteAddress();
        }
        return "Client";
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
    public void sendMessage(@Nonnull String message) throws IOException {
        sendMessage(message, null);
    }

    /**
     * Send a String to the instantiated connection, replacing a message with
     * the same key that is still waiting to be sent.
     * <p>
     * Use a key for a message that gives the whole state of one thing, such
     * as a bean, so that a client that is behind gets only the latest.
     *
     * @param message message to send
     * @param key     the thing the message gives the whole state of, or null
     * @throws IOException if problem sending message
     */
    public void sendMessage(@Nonnull String message, @CheckForNull Object key) throws IOException {
        log.trace("Sending \"{}\"", message);
        sender.send(message, key);
    }

    /**
     * Write a message to the client, waiting if it is behind.
     *
     * @param message message to send
     * @throws IOException if problem sending message
     */
    private void write(@Nonnull String message) throws IOException {
        if (this.dataOutputStream != null) {
            this.dataOutputStream.writeBytes(message);
        } else if (this.session.isOpen()) {
            try {
                // only one thread at a time may call sendString; the
                // sender writes one message at a time
                this.session.getRemote().sendString(message);
            } catch (WebSocketException ex) {
                // A WebSocketException is most likely a broken socket,
                // so rethrow it as an IOException
                if (ex.getMessage() == null) {
                    // provide a generic message if ex has no message
                    throw new IOException(EX_SENDING_MSG, ex);
                }
                throw new IOException(ex);
            } catch (IOException ex) {
                if (ex.getMessage() == null) {
                    // provide a generic message if ex has no message
                    throw new IOException(EX_SENDING_MSG, ex);
                }
                throw ex; // rethrow if complete
            }
        } else {
            // immediately thrown an IOException to trigger closing
            // actions up the call chain
            throw new IOException("Will not send message on non-open session");
        }
    }

    /**
     * Close the connection after a message can't be sent, so the server
     * holding it open ends it.
     */
    private void closeAfterFailure() {
        try {
            close();
        } catch (IOException ex) {
            log.debug("Closing after a failed send: {}", ex.getMessage());
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
