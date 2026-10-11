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

    /**
     * Get the Connection CAN ID.
     * @return Connection CAN ID, defaults to 120
     */
    public int getCanid() {
        return _canid;
    }
    
    /**
     * Set the Connection CAN ID.
     * @param canid CAN ID to use
     */
    public void setCanId(int canid) {
        _canid = canid;
    }

    /**
     * Set the CAN ID by numerical String value.
     * @param canId String value of CAN ID.
     */
    public void setCanId( @javax.annotation.CheckForNull String canId) {
        try {
            setCanId(Integer.parseInt(canId));
        } catch (NumberFormatException e) {
            log.error("Cannot parse CAN ID \"{}\" - check your preference settings", canId, e);
            log.error("Now using CAN ID {}",getCanid());
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrafficController.class);

}
