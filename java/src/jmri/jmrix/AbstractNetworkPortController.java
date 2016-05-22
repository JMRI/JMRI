// AbstractNetworkPortController.java

package jmri.jmrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.net.*;

/**
 * Enables basic setup of a network client interface
 * for a jmrix implementation.
 *
 *
 * @author      Kevin Dickerson  Copyright (C) 2010
 * @author      Based upon work originally done by Paul Bender  Copyright (C) 2009
 * @version	$Revision$
 * @see         jmri.jmrix.NetworkConfigException
 */
abstract public class AbstractNetworkPortController extends AbstractPortController implements NetworkPortAdapter{

    // the host name and port number identify what we are 
    // talking to.
    protected String m_HostName=null;
    protected int m_port=0;
    // keep the socket provides our connection.
    protected Socket socketConn = null;
    
    public void connect(String host, int port) throws Exception {
        setHostName(host);
        setPort(port);
        try {
            connect();
        } catch (Exception e){
            throw e;
        }
    }
    
    public void connect() throws Exception {
        opened=false;
        if (m_HostName==null || m_port==0){
            log.error("No host name or port set :" + m_HostName + ":" + m_port);
            return;
        }
        try {
            socketConn = new Socket(m_HostName,  m_port);
            socketConn.setKeepAlive(true);
            opened = true;
        } catch (Exception e) {
            log.error("error opening network connection: "+e);
            if(m_port!=0){
               ConnectionStatus.instance().setConnectionState(
            		m_HostName+":"+m_port, ConnectionStatus.CONNECTION_DOWN);
            } else {
               ConnectionStatus.instance().setConnectionState(
            		m_HostName, ConnectionStatus.CONNECTION_DOWN);
            }
            throw(e);
        }
            if(opened && m_port!=0){
               ConnectionStatus.instance().setConnectionState(
            		m_HostName+":"+m_port, ConnectionStatus.CONNECTION_UP);
            } else if(opened){
               ConnectionStatus.instance().setConnectionState(
            		m_HostName, ConnectionStatus.CONNECTION_UP);
            }
    }
    
	/** Query the status of this connection.  If all OK, at least
	 * as far as is known, return true */
    public boolean status() {return opened;}

    /**
     * Remember the associated host name
     * @param s
     */
    public void setHostName(String s){
        m_HostName=s;
        if (s.equals("")) m_HostName = JmrixConfigPane.NONE;
    }

    public String getHostName(){
        return m_HostName;
    }

     /**
      * Remeber the associated port number
      * @param p
      **/
	public void setPort(int p){
        m_port=p;
    }
    
    public void setPort(String p){
        m_port=Integer.parseInt(p);
    }
    
	public int getPort(){
        return m_port;
    }
    
    /**
    * Returns the connection name for the network connection in the format of ip_address:port
    * @return ip_address:port
    **/
    public String getCurrentPortName() {
        String t = getHostName();
        int p = getPort();
        if (t != null && !t.equals("")) {
            if (p!=0){
                return t+":"+p;
            }
            return t;
        }
        else return JmrixConfigPane.NONE;
    }
    
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            if(m_port!=0){
               ConnectionStatus.instance().setConnectionState(
            		m_HostName+":"+m_port, ConnectionStatus.CONNECTION_DOWN);
            } else {
               ConnectionStatus.instance().setConnectionState(
            		m_HostName, ConnectionStatus.CONNECTION_DOWN);
            }
        }
        try {
            return new DataInputStream(socketConn.getInputStream());
        } catch (java.io.IOException ex1) {
            log.error("Exception getting input stream: "+ex1);
            return null;
        }
    }
    
    public DataOutputStream getOutputStream() {
        if (!opened) log.error("getOutputStream called before load(), stream not available");
        try {
            return new DataOutputStream(socketConn.getOutputStream());
        }
     	catch (java.io.IOException e) {
            log.error("getOutputStream exception: "+e);
            if(m_port!=0){
               ConnectionStatus.instance().setConnectionState(
            		m_HostName+":"+m_port, ConnectionStatus.CONNECTION_DOWN);
            } else {
               ConnectionStatus.instance().setConnectionState(
            		m_HostName, ConnectionStatus.CONNECTION_DOWN);
            }
     	}
     	return null;
    }
    
    /*This in place here until all systems are converted over to the systemconnection memo
    this will then become abstract, once all the code has been refactored*/
    public SystemConnectionMemo getSystemConnectionMemo() { return null; }
    
    /*Set disable should be handled by the local port controller in each connection
    this is abstract in the Portcontroller and can be removed once all the other codes has
    been refactored */
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
    }
    /*Dispose should be handled by the port adapters and this should be abstract
    However this is in place until all the other code has been refactored */
    public void dispose(){ return; }

    //private boolean allowConnectionRecovery = false;

    /**
     * This is called when a connection is initially lost.  It closes the client side
     * socket connection, resets the open flag and attempts a reconnection.
     */
        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="DE_MIGHT_IGNORE",
                                                justification="we are trying to close a failed connection, it doesn't matter if it generates an error")
    public void recover(){
        if (!allowConnectionRecovery) return;
        opened = false;
        try {
            socketConn.close();
        } catch (Exception e) { }
        reconnect();
    }

    /**
     * Attempts to reconnect to a failed Server
     */
    public void reconnect(){
		
        // If the connection is already open, then we shouldn't try a re-connect.
        if (opened && !allowConnectionRecovery) return;
        reconnectwait thread = new reconnectwait();
        thread.setName("Connection Recovery " + getHostName());
        thread.start();
        try{
            thread.join();
        } catch (InterruptedException e) {
            log.error("Unable to join to the reconnection thread");
        }

        if (!opened){
            log.error("Failed to re-establish connectivity");
        } else {
            resetupConnection();
            log.info("Reconnected to " + getHostName());
        }
    }

    /**
     * Customizable method to deal with resetting a system connection after
     * a sucessful recovery of a connection.
     */
    protected void resetupConnection() {}

    class reconnectwait extends Thread{
        public final static int THREADPASS     = 0;
        public final static int THREADFAIL     = 1;
        int         _status;
        
        public int status() {
            return _status;
        }
        public reconnectwait() {
            _status = THREADFAIL;
        }
        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="DE_MIGHT_IGNORE",
                                                justification="we are testing for a the ability to re-connect and this is likely to generate an error which can be ignored")
        public void run() {
            boolean reply = true;
            int count = 0;
            int secondCount = 0;
            while(reply){
                safeSleep(reconnectinterval, "Waiting");
                count++;
                try {
                    connect();
                } catch (Exception e) {
                }
                reply=!opened;
                if (count >=retryAttempts){
                    log.error("Unable to reconnect after " + count + " Attempts, increasing duration of retries");
                    //retrying but with twice the retry interval.
                    reconnectinterval = reconnectinterval*2;
                    count = 0;
                    secondCount++;
                }
                if (secondCount >=10){
                    log.error("Giving up on reconnecting after 100 attempts");
                    reply=false;
                }
            }
        }
    }
   
    final static protected Logger log = LoggerFactory.getLogger(AbstractNetworkPortController.class.getName());
    
}
