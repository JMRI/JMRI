package jmri.jmrix.loconet.locormi;

import jmri.jmrix.loconet.*;
import com.sun.java.util.collections.LinkedList;

/**
 * Client for the RMI LocoNet server.
 * <p>Copyright: Copyright (c) 2002</p>
 * @author Alex Shepherd, Bob Jacobsen
 * @version $Revision: 1.10 $
 */

public class LnMessageClient extends LnTrafficRouter {

    String                      serverName = null ;
    int                         pollTimeout ;
    LnMessageServerInterface    lnServer = null ;
    LnMessageBufferInterface    lnMessageBuffer = null ;
    LnMessageClientPollThread   pollThread = null ;

    public LnMessageClient() {
        super();
    }

    /**
     * Forward messages to the server.
     */
    public void sendLocoNetMessage(LocoNetMessage m) {
        try{
            if( lnMessageBuffer != null )
                lnMessageBuffer.sendLocoNetMessage( m );
            else
                log.warn( "sendLocoNetMessage: no connection to server" ) ;
        }
        catch( java.rmi.RemoteException ex )
            {
                log.warn( "sendLocoNetMessage: Exception: " + ex );
            }
    }

    // messages that are received from the server should
    // be passed to this.notify(LocoNetMessage m);

    /**
     * Start the connection to the server. This is invoked
     * once.
     */
    public void configureRemoteConnection(String remoteHostName, int timeoutSec) throws LocoNetException {
        serverName = remoteHostName ;
        pollTimeout = timeoutSec * 1000 ;  // convert to ms

        if (log.isDebugEnabled()) log.debug("configureRemoteConnection: "
                                            +remoteHostName+" "+timeoutSec);

        try{
            System.setSecurityManager(new java.rmi.RMISecurityManager());
            log.debug("security manager set, set interface to //"
                      +remoteHostName+"//"
                      +LnMessageServer.serviceName );
            LnMessageServerInterface lnServer = (LnMessageServerInterface) java.rmi.Naming.lookup(
                                                                                                  "//" + serverName + "/" + LnMessageServer.serviceName );

            lnMessageBuffer = lnServer.getMessageBuffer() ;
            lnMessageBuffer.enable( 0 );
            pollThread = new LnMessageClientPollThread( this ) ;
        }
        catch( Exception ex ){
            log.error( "Exception: " + ex );
            throw new LocoNetException( "Failed to Connect to Server: " + serverName ) ;
        }
    }

    /**
     * set up all of the other objects to operate with a server
     * connected to this application
     */
    public void configureLocalServices() {
        // This is invoked on the LnMessageClient after it's
        // ready to go, connection running, etc.

        // If a jmri.Programmer instance doesn't exist, create a
        // loconet.SlotManager to do that
        if (jmri.InstanceManager.programmerManagerInstance() == null)
            jmri.jmrix.loconet.SlotManager.instance();

        // do the common manager config
        jmri.jmrix.loconet.LnPortController.configureManagers();

        // the serial connections (LocoBuffer et al) start
        // various threads here.
    }

    public static void main( String[] args ){
    	String logFile = "default.lcf";
    	try {
            if (new java.io.File(logFile).canRead()) {
                org.apache.log4j.PropertyConfigurator.configure("default.lcf");
            } else {
                org.apache.log4j.BasicConfigurator.configure();
            }
        }
        catch (java.lang.NoSuchMethodError e) { System.out.println("Exception starting logging: "+e); }

        try{
            String serverName = java.net.InetAddress.getLocalHost().getHostName();
            LnMessageClient lnClient = new LnMessageClient() ;
            lnClient.configureRemoteConnection( serverName, 60 );

            // Now just site and wait for the Thread to read
            synchronized( lnClient ){
                lnClient.wait() ;
            }
        }
        catch( Exception ex ){
            System.out.println( "Exception: " + ex ) ;
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnMessageClient.class.getName());
}
