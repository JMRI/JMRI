// XNetSimulatorPortController.java

package jmri.jmrix.lenz;

import org.apache.log4j.Logger;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a XNet communications port
 * <p>
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @author			Paul Bender    Copyright (C) 2004,2010
 * @version			$Revision$
 */
public abstract class XNetSimulatorPortController extends jmri.jmrix.AbstractSerialPortController implements XNetPortController {

    public XNetSimulatorPortController(){
       super();
       adaptermemo = new XNetSystemConnectionMemo();
    }

    // base class. Implementations will provide InputStream and OutputStream
    // objects to XNetTrafficController classes, who in turn will deal in messages.    
    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();
    
    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();
    
    /**
     * Check that this object is ready to operate. This is a question
     * of configuration, not transient hardware status.
     */
    public abstract boolean status();
    
    /**
     * Can the port accept additional characters?  This might
     * go false for short intervals, but it might also stick
     * off if something goes wrong.
     */
    public abstract boolean okToSend();
    
    /**
     * We need a way to say if the output buffer is empty or not
     */
    public abstract void setOutputBufferEmpty(boolean s);
   
    protected XNetSystemConnectionMemo adaptermemo = null;

    @Override
    public jmri.jmrix.SystemConnectionMemo getSystemConnectionMemo(){
        if(adaptermemo!=null){
          log.debug("adapter memo not null");
          return adaptermemo;
        }
        else
        {
          log.debug("adapter memo null");
          return null;
	}
    }

    public void dispose(){
       adaptermemo.dispose();
    }
    
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }

    static Logger log = Logger.getLogger(XNetSimulatorPortController.class.getName());


}


/* @(#)XNetSimulatorPortController.java */
