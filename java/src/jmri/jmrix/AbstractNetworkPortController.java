// AbstractNetworkPortController.java

package jmri.jmrix;

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
        if (m_HostName==null || m_port==0){
            log.error("No host name or port set :" + m_HostName + ":" + m_port);
            return;
        }
        try {
            socketConn = new Socket(m_HostName,  m_port);
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
    
	public int getPort(){
        return m_port;
    }
        
    public String getCurrentPortName() {
        return ""+m_port;
    }
    
    public void setPort(String p){
        m_port=Integer.parseInt(p);
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
   
    final static protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractNetworkPortController.class.getName());
    
}
