// XNetSerialPortController.java

package jmri.jmrix.lenz;

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
public abstract class XNetSerialPortController extends jmri.jmrix.AbstractSerialPortController implements XNetPortController {

    public XNetSerialPortController(){
        super();
        //option2Name = "Buffer";
        //options.put(option2Name, new Option(option2Name, "Check Buffer : ", validOption2));
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
   

    /* Option 2 is not currently used with RxTx 2.0.  In the past, it
       was used for the "check buffer status when sending" If this is still set        in a configuration file, we need to handle it, but we are not writing it        to new configuration files. */
    /*public String getCurrentOption2Setting() {
        if(options.get(option2Name).getCurrent()==null) return("no");
        else return options.get(option2Name).getCurrent();
    }*/

    protected String [] validOption2 = new String[]{"yes", "no"};
    protected boolean checkBuffer = false;

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
      adaptermemo=null;
    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetSerialPortController.class.getName());


}


/* @(#)XNetSerialPortController.java */
