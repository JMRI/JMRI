// TrafficController.java

package jmri.jmrix.can;

/**
 * Traffic controller for CAN access.
 *
 * @author          Bob Jacobsen Copyright (C) 2008
 * @version			$Revision: 1.3 $
 */
abstract public class TrafficController extends AbstractCanTrafficController {
    
    public TrafficController() {
        super();
    }
   
    /**
     * static function returning the CanTrafficController instance to use.
     * @return The registered TrafficController instance for general use,
     *         which must have been initialized previously
     */
    static public TrafficController instance() {
        return self;
    }
    
    static protected TrafficController self = null;
    protected void setInstance() { self = this; }

    // The CAN ID to be used by the hardware
    protected static int _canid = 120;
    public int getCanid() { return _canid; }

}


/* @(#)GcTrafficController.java */

