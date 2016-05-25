// TrafficController.java
package jmri.jmrix.can;

/**
 * Traffic controller for CAN access.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @version	$Revision$
 */
abstract public class TrafficController extends AbstractCanTrafficController {

    public TrafficController() {
        super();
    }

    protected int _canid = 120;

    public int getCanid() {
        return _canid;
    }

    public void setCanId(int canid) {
        _canid = canid;
    }

}


/* @(#)GcTrafficController.java */
