// AbstractNetworkPortController.java

package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.swing.JOptionPane;

import java.net.*;

/**
 * Enables basic setup of a network client interface
 * for a jmrix implementation.
 *
 *
 * @author      Kevin Dickerson  Copyright (C) 2010
 * @author      Based upon work originally done by Paul Bender  Copyright (C) 2009
 * @version	$Revision: 1.1 $
 * @see         jmri.jmrix.AbstractNetworkConfigException
 */
abstract public class AbstractNetworkPortController extends AbstractPortController implements NetworkPortAdapter{

    // the host name and port number identify what we are 
    // talking to.
    protected String m_HostName=null;
    protected int m_port=0;
    // keep the socket provides our connection.
    protected Socket socketConn = null;
    
    public void connect(String host, int port) {
        setHostName(host);
        setPort(port);
        connect();
    }
    
    public void connect(){
        if (m_HostName==null || m_port==0){
            log.error("No host name or port set :" + m_HostName + ":" + m_port);
            return;
        }
        try {
            socketConn = new Socket(m_HostName,  m_port);
            opened = true;
        } catch (Exception e) {
            log.error("error opening network connection: "+e);
            ConnectionStatus.instance().setConnectionState(
            		m_HostName, ConnectionStatus.CONNECTION_DOWN);
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
        if (s.equals("")) m_HostName = "(none)";
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
            ConnectionStatus.instance().setConnectionState(
            		m_HostName, ConnectionStatus.CONNECTION_DOWN);
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
            ConnectionStatus.instance().setConnectionState(
            		m_HostName, ConnectionStatus.CONNECTION_DOWN);
     	}
     	return null;
    }
   
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractNetworkPortController.class.getName());
    
}
