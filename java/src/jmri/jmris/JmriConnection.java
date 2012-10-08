package jmri.jmris;

import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.jetty.websocket.WebSocket.Connection;

/**
 * Abstraction of DataOutputStream and WebSocket.Connection classes
 * 
 * Used so that that server objects need only to use a single object/method
 * to send data to any supported object type.
 * 
 * @author rhwood Randall Wood Copyright (C) 2012
 */
public class JmriConnection {

	private Connection webSocketConnection = null;
    private DataOutputStream dataOutputStream = null;

    /**
     * Create a JmriConnection that sends output to a WebSocket
     * 
     * @param connection
     */
    public JmriConnection(Connection connection) {
    	this.webSocketConnection = connection;
    }

    /**
     * Create a JmriConnection that sends output to a DataOutputStream
     * 
     * @param output
     */
    public JmriConnection(DataOutputStream output) {
    	this.dataOutputStream = output;
    }

    public Connection getWebSocketConnection() {
		return webSocketConnection;
	}

	public void setWebSocketConnection(Connection webSocketConnection) {
		this.webSocketConnection = webSocketConnection;
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
    	if (this.dataOutputStream != null) {
    		this.dataOutputStream.writeBytes(message);
    	} else if (this.webSocketConnection != null) {
    		this.webSocketConnection.sendMessage(message);
    	}
    }
    
    public void close() throws IOException {
    	if (this.dataOutputStream != null) {
    		this.dataOutputStream.close();
    	} else if (this.webSocketConnection != null) {
    		this.webSocketConnection.close();
    	}
    }
}
