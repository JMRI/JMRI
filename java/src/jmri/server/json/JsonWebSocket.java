package jmri.server.json;

import java.io.IOException;
import java.net.SocketTimeoutException;
import jmri.InstanceManager;
import jmri.implementation.QuietShutDownTask;
import jmri.jmris.json.JSON;
import jmri.jmris.json.JsonServerPreferences;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
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

    private final static Logger log = LoggerFactory.getLogger(JsonWebSocket.class);
    private JsonConnection connection;
    private JsonClientHandler handler;
    private QuietShutDownTask shutDownTask;

    @OnWebSocketConnect
    public void onOpen(Session sn) {
        log.debug("Opening connection");
        try {
            this.connection = new JsonConnection(sn);
            sn.setIdleTimeout((long) (JsonServerPreferences.getDefault().getHeartbeatInterval() * 1.1));
            this.handler = new JsonClientHandler(this.connection);
            this.shutDownTask = new QuietShutDownTask("Close open web socket") { // NOI18N
                @Override
                public boolean execute() {
                    try {
                        JsonWebSocket.this.getConnection().sendMessage(JsonWebSocket.this.getConnection().getObjectMapper().createObjectNode().put(JSON.TYPE, JSON.GOODBYE));
                    } catch (IOException e) {
                        log.warn("Unable to send goodbye while closing socket.\nError was {}", e.getMessage());
                    }
                    JsonWebSocket.this.getConnection().getSession().close();
                    return true;
                }
            };
            log.debug("Sending hello");
            this.handler.sendHello(JsonServerPreferences.getDefault().getHeartbeatInterval());
        } catch (IOException e) {
            log.warn("Error opening WebSocket:\n{}", e.getMessage());
            sn.close();
        }
        InstanceManager.getDefault(jmri.ShutDownManager.class).register(this.shutDownTask);
    }

    @OnWebSocketClose
    public void onClose(int i, String string) {
        log.debug("Closing connection because {} ({})", string, i);
        this.handler.dispose();
        InstanceManager.getDefault(jmri.ShutDownManager.class).deregister(this.shutDownTask);
    }

    @OnWebSocketError
    public void onError(Throwable thrwbl) {
        if (thrwbl instanceof SocketTimeoutException) {
            this.connection.getSession().close(StatusCode.NO_CLOSE, thrwbl.getMessage());
        } else {
            log.error(thrwbl.getMessage(), thrwbl);
        }
    }

    @OnWebSocketMessage
    public void onMessage(String string) {
        try {
            this.handler.onMessage(string);
        } catch (IOException e) {
            log.error("Error on WebSocket message:\n{}", e.getMessage());
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
