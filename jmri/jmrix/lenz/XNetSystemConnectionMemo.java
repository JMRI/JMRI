//XNetSystemConnectionMemo.java

package jmri.jmrix.lenz;

import jmri.*;

/**
 * Lightweight class to denote that a system is active
 * and provide general information
 * <p>
 * Objects of specific subtypes are registered in the 
 * instance manager to activate their particular system.
 *
 * @author   Paul Bender Copyright (C) 2010
 * @version  $Revision: 1.5 $
 */

public class XNetSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

   public XNetSystemConnectionMemo(XNetTrafficController xt){
     super("X","XPressnet");
     this.xt=xt;
     xt.setSystemConnectionMemo(this);
     register(); // registers general type
     InstanceManager.store(this,XNetSystemConnectionMemo.class); // also register as specific type

     // create and register the XNetComponentFactory
     InstanceManager.store(cf=new jmri.jmrix.lenz.swing.XNetComponentFactory(this),
                           jmri.jmrix.swing.ComponentFactory.class);

     if(log.isDebugEnabled()) log.debug("Created XNetSystemConnectionMemo");
   }

   public XNetSystemConnectionMemo(){
     super("X","XPressnet");
     register(); // registers general type
     InstanceManager.store(this,XNetSystemConnectionMemo.class); // also register as specific type

     // create and register the XNetComponentFactory
     InstanceManager.store(cf=new jmri.jmrix.lenz.swing.XNetComponentFactory(this),                            jmri.jmrix.swing.ComponentFactory.class);

     if(log.isDebugEnabled()) log.debug("Created XNetSystemConnectionMemo");
   }

   jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public XNetTrafficController getXNetTrafficController() { return xt; }
    private XNetTrafficController xt;
    public void setXNetTrafficController(XNetTrafficController xt) { 
               this.xt = xt; 
               // in addition to setting the traffic controller in this object,
               // set the systemConnectionMemo in the traffic controller
               xt.setSystemConnectionMemo(this);
    }

    /**
     * Provides access to the Programmer for this particular connection.
     */
    public ProgrammerManager getProgrammerManager() {
        if (programmerManager == null)
            programmerManager = new XNetProgrammerManager(new XNetProgrammer(xt),this);
        return programmerManager;
    }
    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }

    private ProgrammerManager programmerManager;

    public void dispose() {
        xt = null;
        InstanceManager.deregister(this, XNetSystemConnectionMemo.class);
        if (cf != null)
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        super.dispose();
    }


        static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetSystemConnectionMemo.class.getName());


}
/* @(#)XNetSystemConnectionMemo.java */
