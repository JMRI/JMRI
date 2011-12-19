// XNetNetworkPortController.java

package jmri.jmrix.lenz;

/**
 * Abstract base for classes representing a XNet communications port
 * <p>
 *
 * @author			Bob Jacobsen    Copyright (C) 2001, 2008
 * @author			Paul Bender    Copyright (C) 2004,2010
 * @version			$Revision$
 */
public abstract class XNetNetworkPortController extends jmri.jmrix.AbstractNetworkPortController implements XNetPortController {

    public XNetNetworkPortController(){
       super();
       adaptermemo = new XNetSystemConnectionMemo();
    }

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
    public void setOutputBufferEmpty(boolean s) {} // Maintained for compatibility with XNetPortController. Simply ignore calls !!!

    /* Option 2 is not currently used with RxTx 2.0.  In the past, it
       was used for the "check buffer status when sending" If this is still set        in a configuration file, we need to handle it, but we are not writing it        to new configuration files. */
    public String getCurrentOption2Setting() {
        if(mOpt2==null) return("no");
        else return mOpt2;
    }

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
       log.error("Dispose called");
    }
    
    public void setDisabled(boolean disabled) { 
        mDisabled = disabled;
        if(adaptermemo!=null)
            adaptermemo.setDisabled(disabled);
    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetNetworkPortController.class.getName());


}


/* @(#)XNetNetworkPortController.java */
