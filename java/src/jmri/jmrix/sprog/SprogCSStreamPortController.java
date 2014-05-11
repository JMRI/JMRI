// SprogCSStreamPortController.java

package jmri.jmrix.sprog;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import jmri.jmrix.AbstractStreamPortController;
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
public class SprogCSStreamPortController extends AbstractStreamPortController implements SprogInterface {

    public SprogCSStreamPortController(DataInputStream in,DataOutputStream out,String pname){
        super(in,out,pname);
        adaptermemo = new SprogSystemConnectionMemo(SprogConstants.SprogMode.OPS);
        
    }

    @Override
    public void configure() {
       log.debug("configure() called.");
       SprogTrafficController control = SprogTrafficController.instance();
       
       // connect to the traffic controller
       ((SprogSystemConnectionMemo)adaptermemo).setSprogTrafficController(control);
       control.setAdapterMemo((SprogSystemConnectionMemo)adaptermemo);
       ((SprogSystemConnectionMemo)adaptermemo).configureManagers();
       control.connectPort(this);

       // declare up
       ActiveFlag.setActive(); 

    }


    /**
     * Check that this object is ready to operate. This is a question
     * of configuration, not transient hardware status.
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
    public void addSprogListener( SprogListener l){
      SprogTrafficController.instance().addSprogListener(l);
    }

    @Override
    public void removeSprogListener( SprogListener l) {
      SprogTrafficController.instance().removeSprogListener(l);
    }

    @Override
    public void sendSprogMessage(SprogMessage m, SprogListener l){
      SprogTrafficController.instance().sendSprogMessage(m,l);
    }
    
    static Logger log = LoggerFactory.getLogger(SprogCSStreamPortController.class.getName());


}


/* @(#)SprogCSStreamPortController.java */
