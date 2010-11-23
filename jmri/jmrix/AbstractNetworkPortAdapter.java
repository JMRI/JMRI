// AbstractNetworkPortAdapter.java

package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.net.*;

/**
 * Enables basic setup of a network client interface
 * for a jmrix implementation.
 *
 *
 * @author      Paul Bender  Copyright (C) 2009
 * @version	$Revision: 1.3 $
 * @see         jmri.jmrix.NetworkConfigException
 */
@Deprecated
abstract public class AbstractNetworkPortAdapter extends AbstractSerialPortController{

        // the host name and port number identify what we are 
        // talking to.
        protected String m_HostName=null;
        protected int m_port=0;
        // keep the socket provides our connection.
        protected Socket socketConn = null;
        // external ends of the sockets 
        private DataOutputStream pout=null; // for output to other classes
        private DataInputStream pin = null; // for input from other classes    

	/** Open a specified port.  The appname argument is to be provided to 
         * the underlying OS during startup so that it can show on status 
         * displays, etc
	 */
        public String openPort(String portName, String appName)  {
           return openPort(m_HostName,m_port,appName);
        }

	/** Open an IP port.  The appname argument is to be provided to 
         * the underlying OS during startup so that it can show on status 
         * displays, etc
	 */
	public String openPort(String ipAddress,int port, String appName){
	setHostName(ipAddress);
	setPort(port);
        try {
            socketConn = new Socket(ipAddress,port);
            pout=new DataOutputStream (socketConn.getOutputStream());
            pin=new DataInputStream( socketConn.getInputStream());
        } catch (java.net.UnknownHostException e) {
            log.error("Invalid Host Name " +e.toString() );
            ConnectionStatus.instance().setConnectionState(ipAddress+":"+port, ConnectionStatus.CONNECTION_DOWN);
            return "Unexpected error while opening TCP connection with "+ipAddress+":"+port+": "+e;
        } catch (java.io.IOException ex) {
            log.error("init (network): Exception: " +ex.toString());
            ex.printStackTrace();
            ConnectionStatus.instance().setConnectionState(ipAddress+":"+port, ConnectionStatus.CONNECTION_DOWN);
            return "Unexpected error while opening TCP connection with "+ipAddress+":"+port+": "+ex;
        }

        ConnectionStatus.instance().setConnectionState(ipAddress+":"+port, ConnectionStatus.CONNECTION_UP);
        return null;
}

	/** Query the status of this connection.  If all OK, at least
	 * as far as is known, return true */
        public boolean status() {return (pout!=null && pin!=null);}

    /**
     * Remember the associated host name
     * @param s
     */
    public void setHostName(String s){
        m_HostName=s;
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
	
    // base class methods for the PortAdapter interface
    public DataInputStream getInputStream() {
        if (pin == null ) {
            log.error("getInputStream called before load(), stream not available");         
            throw(new java.lang.NullPointerException());
        }
        return pin;
    }
    
    public DataOutputStream getOutputStream() {
        if (pout==null) {
            log.error("getOutputStream called before load(), stream not available");
            throw(new java.lang.NullPointerException());
        }
     	return pout;
    }
    
    
    /**
     * Get an array of valid baud rates. This is currently just a message
     * saying its fixed
     */
    public String[] validBaudRates() {
        log.error("Unexpected call to validBaudRates()");
        return null;
    }
    
    public SystemConnectionMemo getSystemConnectionMemo() { return null; }
    
    public void dispose(){
    }
   
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractNetworkPortAdapter.class.getName());
    
}
