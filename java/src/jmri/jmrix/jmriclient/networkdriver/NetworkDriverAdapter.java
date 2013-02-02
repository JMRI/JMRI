// NetworkDriverAdapter.java

package jmri.jmrix.jmriclient.networkdriver;

import org.apache.log4j.Logger;
import jmri.jmrix.jmriclient.JMRIClientPortController;
import jmri.jmrix.jmriclient.JMRIClientTrafficController;

/**
 * Implements NetworkPortAdapter for the jmriclient system network connection.
 * <P>This connects
 * a JMRI server (daemon) via a telnet connection.
 *
 * @author	Paul Bender Copyright (C) 2010
 * @version	$Revision$
 */
public class NetworkDriverAdapter extends JMRIClientPortController {

    public NetworkDriverAdapter() {
        super();
        adaptermemo = new jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo();
        setPort(2048); // set the default port on construction
    }

    /**
     * set up all of the other objects to operate with an JMRI
     * server connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        JMRIClientTrafficController control = new JMRIClientTrafficController();
        control.connectPort(this);
        adaptermemo.setJMRIClientTrafficController(control);
        adaptermemo.configureManagers();
        //adaptermemo.configureCommandStation();

        // mark OK for menus
        jmri.jmrix.jmriclient.ActiveFlag.setActive();
    }
    
    public boolean status() {return opened;}

    public jmri.jmrix.SystemConnectionMemo getSystemConnectionMemo() { return adaptermemo; }

    // private control members
    private boolean opened = false;

    public void dispose(){
        adaptermemo.dispose();
        adaptermemo = null;
    }

    static public NetworkDriverAdapter instance() {
        log.error("Instance Called");
        new java.lang.Exception().printStackTrace(); 
        if (mInstance == null){
            // create a new one and initialize
            NetworkDriverAdapter m = new NetworkDriverAdapter();
            m.setManufacturer(jmri.jmrix.DCCManufacturerList.JMRI);
            
            // and set as instance
            mInstance = m;
        }
        return mInstance;
    }
    static NetworkDriverAdapter mInstance = null;

    static Logger log = Logger.getLogger(NetworkDriverAdapter.class.getName());

}
