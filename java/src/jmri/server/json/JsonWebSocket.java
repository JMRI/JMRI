package jmri.server.json;

import java.io.IOException;
import java.net.SocketTimeoutException;
import jmri.InstanceManager;
import jmri.implementation.QuietShutDownTask;
import jmri.jmris.json.JsonServerPreferences;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Randall Wood Copyright (C) 2012, 2013, 2016
 */
@WebSocket
public class JsonWebSocket {

    private static final Logger log = LoggerFactory.getLogger(JsonWebSocket.class);
    private JsonConnection connection;
    private JsonClientHandler handler;
    private QuietShutDownTask shutDownTask;

    @OnWebSocketConnect
    public void onOpen(Session sn) {
        log.debug("Opening connection");
        try {
            this.connection = new JsonConnection(sn);
            sn.setIdleTimeout((long) (InstanceManager.getDefault(JsonServerPreferences.class).getHeartbeatInterval() * 1.1));
            this.handler = new JsonClientHandler(this.connection);
            this.shutDownTask = new QuietShutDownTask("Close open web socket") { // NOI18N
                @Override
                public boolean execute() {
                    try {
                        JsonWebSocket.this.getConnection().sendMessage(JsonWebSocket.this.getConnection().getObjectMapper().createObjectNode().put(JSON.TYPE, JSON.GOODBYE), 0);
                    } catch (IOException e) {
                        log.warn("Unable to send goodbye while closing socket.\nError was {}", e.getMessage());
                    }
                    JsonWebSocket.this.getConnection().getSession().close();
                    return true;
                }
            };
            log.debug("Sending hello");
            this.handler.onMessage(JsonClientHandler.HELLO_MSG);
        } catch (IOException e) {
            log.warn("Error opening WebSocket:\n{}", e.getMessage());
            sn.close();
        }
        InstanceManager.getDefault(jmri.ShutDownManager.class).register(this.shutDownTask);
    }

    @OnWebSocketClose
    public void onClose(int i, String string) {
        log.debug("Closing connection because {} ({})", string, i);
        this.handler.onClose();
        InstanceManager.getDefault(jmri.ShutDownManager.class).deregister(this.shutDownTask);
    }

    @OnWebSocketError
    public void onError(Throwable thrwbl) {
        if (thrwbl instanceof SocketTimeoutException) {
            this.connection.getSession().close(StatusCode.NO_CLOSE, thrwbl.getMessage());
        } else if (thrwbl instanceof EofException || thrwbl instanceof WebSocketException) {
            try {
                this.connection.getSession().disconnect();
            } catch (IOException ex) {
                this.onClose(StatusCode.ABNORMAL, thrwbl.getMessage());
            }
        } else {
            log.error("Unanticipated error {}", thrwbl.getMessage(), thrwbl);
        }
    }

    @OnWebSocketMessage
    public void onMessage(String string) {
        try {
            this.handler.onMessage(string);
        } catch (IOException e) {
            if(!e.getMessage().equals("Will not send message on non-open session")) {
               // This exception did not occured because the connection is
               // either closing or already closed, so log it.
               log.error("Error on WebSocket message:\n{}", e.getMessage());
            }
            this.connection.getSession().close();
            InstanceManager.getDefault(jmri.ShutDownManager.class).deregister(this.shutDownTask);
        }
    }

    /**
     * @return the connection
     */
    protected JsonConnection getConnection() {
        return connection;
    }

}
