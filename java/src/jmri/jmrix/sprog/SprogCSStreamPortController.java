// SprogCSStreamPortController.java

package jmri.jmrix.sprog;

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
public class SprogCSStreamPortController extends jmri.jmrix.AbstractStreamPortController implements SprogInterface {

    private DataInputStream input;
    private DataOutputStream output;

    public SprogCSStreamPortController(DataInputStream in,DataOutputStream out,String pname){
        super(in,out,pname);
        adaptermemo = new jmri.jmrix.sprog.SprogSystemConnectionMemo(SprogConstants.SprogMode.OPS);
        
    }

    public void configure() {
       log.debug("configure() called.");
       SprogTrafficController control = SprogTrafficController.instance();
       
       // connect to the traffic controller
       ((SprogSystemConnectionMemo)adaptermemo).setSprogTrafficController(control);
       control.setAdapterMemo((SprogSystemConnectionMemo)adaptermemo);
       ((SprogSystemConnectionMemo)adaptermemo).configureManagers();
       control.connectPort(this);

       // declare up
       jmri.jmrix.sprog.ActiveFlag.setActive(); 

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

    public void addSprogListener( SprogListener l){
      SprogTrafficController.instance().addSprogListener(l);
    }

    public void removeSprogListener( SprogListener l) {
      SprogTrafficController.instance().removeSprogListener(l);
    }

    public void sendSprogMessage(SprogMessage m, SprogListener l){
      SprogTrafficController.instance().sendSprogMessage(m,l);
    }
    
    static Logger log = LoggerFactory.getLogger(SprogCSStreamPortController.class.getName());


}


/* @(#)SprogCSStreamPortController.java */
