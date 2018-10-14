package jmri.jmrix.can;

/**
 * Traffic controller for CAN access.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
abstract public class TrafficController extends AbstractCanTrafficController {

    /**
     * Create a new CAN TrafficController instance.
     */
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
