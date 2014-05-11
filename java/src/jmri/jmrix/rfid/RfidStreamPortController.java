// RfidStreamPortController.java

package jmri.jmrix.rfid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import jmri.jmrix.AbstractStreamPortController;
import jmri.jmrix.rfid.generic.standalone.SpecificSystemConnectionMemo;
import jmri.jmrix.rfid.generic.standalone.SpecificTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for classes representing a RFID communications port
 * <p>
 * NOTE: This currently only supports the standalone RFID interfaces.
 * <p>
 *
 * @author			Paul Bender    Copyright (C) 2014
 * @version			$Revision$
 */
public class RfidStreamPortController extends AbstractStreamPortController implements RfidInterface {

    public RfidStreamPortController(DataInputStream in,DataOutputStream out,String pname){
        super(in,out,pname);
        adaptermemo = new SpecificSystemConnectionMemo();
        
    }

    @Override
    public void configure() {
       log.debug("configure() called.");
       RfidTrafficController control = new SpecificTrafficController((RfidSystemConnectionMemo)adaptermemo);
       
       // connect to the traffic controller
       ((RfidSystemConnectionMemo)adaptermemo).setRfidTrafficController(control);
       control.setAdapterMemo((RfidSystemConnectionMemo)adaptermemo);
       ((RfidSystemConnectionMemo)adaptermemo).configureManagers();
       control.connectPort(this);

       // declare up
       ActiveFlag.setActive(); 

    }


    /**
     * Check that this object is ready to operate. This is a question
     * of configuration, not transient hardware status.
     * @return true
     */
    @Override
    public boolean status(){ return true; }

    
    /**
     * Can the port accept additional characters?  
     * @return true
     */
    public boolean okToSend(){
                return(true);
    }

    // RFID Interface methods.

    @Override
    public void addRfidListener( RfidListener l){
      ((RfidSystemConnectionMemo)adaptermemo).getTrafficController().addRfidListener(l);
    }

    @Override
    public void removeRfidListener( RfidListener l) {
      ((RfidSystemConnectionMemo)adaptermemo).getTrafficController().removeRfidListener(l);
    }

    @Override
    public void sendRfidMessage(RfidMessage m, RfidListener l){
      ((RfidSystemConnectionMemo)adaptermemo).getTrafficController().sendRfidMessage(m,l);
    }
    
    static Logger log = LoggerFactory.getLogger(RfidStreamPortController.class.getName());


}


/* @(#)RfidStreamPortController.java */
