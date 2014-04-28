// RfidStreamPortController.java

package jmri.jmrix.rfid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;


/**
 * Abstract base for classes representing a RFID communications port
 * <p>
 * NOTE: This currently only supports the standalone RFID interfaces.
 * <p>
 *
 * @author			Paul Bender    Copyright (C) 2014
 * @version			$Revision$
 */
public class RfidStreamPortController extends jmri.jmrix.AbstractStreamPortController implements RfidInterface {

    private DataInputStream input;
    private DataOutputStream output;

    public RfidStreamPortController(DataInputStream in,DataOutputStream out,String pname){
        super(in,out,pname);
        adaptermemo = new jmri.jmrix.rfid.generic.standalone.SpecificSystemConnectionMemo();
        
    }

    public void configure() {
       log.debug("configure() called.");
       RfidTrafficController control = null;
       control = new jmri.jmrix.rfid.generic.standalone.SpecificTrafficController((RfidSystemConnectionMemo)adaptermemo);
       
       // connect to the traffic controller
       ((RfidSystemConnectionMemo)adaptermemo).setRfidTrafficController(control);
       control.setAdapterMemo((RfidSystemConnectionMemo)adaptermemo);
       ((RfidSystemConnectionMemo)adaptermemo).configureManagers();
       control.connectPort(this);

       // declare up
       jmri.jmrix.rfid.ActiveFlag.setActive(); 

    }


    /**
     * Check that this object is ready to operate. This is a question
     * of configuration, not transient hardware status.
     */
    public boolean status(){ return true; }

    
    /**
     * Can the port accept additional characters?  
     */
    public boolean okToSend(){
                return(true);
    }

    // RFID Interface methods.

    public void addRfidListener( RfidListener l){
      ((RfidSystemConnectionMemo)adaptermemo).getTrafficController().addRfidListener(l);
    }

    public void removeRfidListener( RfidListener l) {
      ((RfidSystemConnectionMemo)adaptermemo).getTrafficController().removeRfidListener(l);
    }

    public void sendRfidMessage(RfidMessage m, RfidListener l){
      ((RfidSystemConnectionMemo)adaptermemo).getTrafficController().sendRfidMessage(m,l);
    }
    
    static Logger log = LoggerFactory.getLogger(RfidStreamPortController.class.getName());


}


/* @(#)RfidStreamPortController.java */
