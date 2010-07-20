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
 * @version  $Revision: 1.1 $
 */

public class XNetSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

   public XNetSystemConnectionMemo(XNetTrafficController xt){
     super("X","XPressnet");
     this.xt=xt;
     register(); // registers general type
     InstanceManager.store(this,XNetSystemConnectionMemo.class); // also register as specific type

     // create and register the ComponentFactory
     //Instancemanager.store(cf=new jmri.jmrix.lenz.swing.ComponentFactory(this),
     //                      jmri.jmrix.swing.ComponentFactor.class);

   }

   public XNetSystemConnectionMemo(){
     super("X","XPressnet");
     register(); // registers general type
     InstanceManager.store(this,XNetSystemConnectionMemo.class); // also register as specific type

     // create and register the ComponentFactory
     //Instancemanager.store(cf=new jmri.jmrix.lenz.swing.ComponentFactory(this),                          
     //                      jmri.jmrix.swing.ComponentFactor.class);

   }

   //jmri.jmrix.swing.ComponentFactor cf = null;

    /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public XNetTrafficController getXNetTrafficController() { return xt; }
    private XNetTrafficController xt;
    public void setXNetTrafficController(XNetTrafficController xt) { this.xt = xt; }

    public void dispose() {
        xt = null;
        InstanceManager.deregister(this, XNetSystemConnectionMemo.class);
        //if (cf != null)
        //    InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        super.dispose();
    }




}
/* @(#)XNetSystemConnectionMemo.java */
