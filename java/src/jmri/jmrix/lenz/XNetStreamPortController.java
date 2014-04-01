// XNetStreamPortController.java

package jmri.jmrix.lenz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;


/**
 * Abstract base for classes representing a XNet communications port
 * <p>
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @author			Paul Bender    Copyright (C) 2004,2010,2014
 * @version			$Revision$
 */
public class XNetStreamPortController extends jmri.jmrix.AbstractStreamPortController implements XNetPortController {

    private DataInputStream input;
    private DataOutputStream output;

    public XNetStreamPortController(DataInputStream in,DataOutputStream out,String pname){
        super(in,out,pname);
        adaptermemo = new XNetSystemConnectionMemo();
    }

    public void configure() {
        // connect to a packetizing traffic controller
        XNetTrafficController packets = new XNetPacketizer(new LenzCommandStation());
        packets.connectPort(this);

        ((XNetSystemConnectionMemo)adaptermemo).setXNetTrafficController(packets);

        new XNetInitilizationManager((XNetSystemConnectionMemo)adaptermemo);

        jmri.jmrix.lenz.ActiveFlag.setActive();
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
    
    /**
     * we need a way to say if the output buffer is empty or full
     * this should only be set to false by external processes
     **/
    synchronized public void setOutputBufferEmpty(boolean s)
    {
    }
   
    static Logger log = LoggerFactory.getLogger(XNetStreamPortController.class.getName());


}


/* @(#)XNetStreamPortController.java */
