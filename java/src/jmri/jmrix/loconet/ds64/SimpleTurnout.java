package jmri.jmrix.loconet.ds64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a simple object to track a turnout number and the position of that
 * turnout.
 * <p>
 * Turnout numbering is the same as seen on a Digitrax throttle display; Tools
 * using values from objects of this type must provide the appropriate transform
 * to create turnout numbering which is suitable for use within LocoNet messaging.
 *
 * @author B. Milhaupt Copyright (C) 2011, 2012, 2013, 2014, 2015
 */
public class SimpleTurnout {
    private Integer address;
    private boolean isClosed;
    private boolean isUnused;

    /**
     * Constructor used if address is unknown.  The constructor creates an object
     * which is marked as "unused".
     */
    SimpleTurnout() {
        address = -1;
        isClosed = false;
        isUnused = true;
    }

    /**
     * Constructor used when the turnout address and position are known.
     * <p>
     * Validity state is assumed to be "valid".
     *
     * @param addr turnout address
     * @param closed true if turnout is closed, else false
     */
    public SimpleTurnout(Integer addr, boolean closed) {
        address = addr;
        isClosed = closed;
        isUnused = false;
    }

    /**
     * Constructor used when the turnout address, position, and validity state
     * are known by the instantiating method.
     *
     * @param addr turnout address
     * @param closed true if turnout is closed, else false
     * @param unused trud if turnout is unused, else false
     */
    public SimpleTurnout(Integer addr, boolean closed, boolean unused) {
        address = addr;
        isClosed = closed;
        isUnused = unused;
    }

    /**
     * Get the "validity" state of the simpleTurnout object.  This "validity"
     * state does not necessarily reflect the "validity" state of a physical turnout.
     *
     * @return true if the address is valid
     */
    public boolean isValid() {
        if ((address >=1) && (address <=2048)) {
            return true;
        }
        else
            return false;
    }

    /**
     * Sets the turnout address of the simpleTurnout object.
     *
     * @param addr  address value
     */
    public void setAddress(Integer addr) {
        address = addr;
        if (isValid() == false) {
            log.debug("simpleTurnout says {} is invalid therefore unused", addr);
            isUnused = true;
            isClosed = true;
        }
        else {
            isUnused = false;
        }
    }

    /**
     * Returns the address field of the simpleTurnout object.
     *
     * @return the address from the GUI element
     */
    public Integer getAddress() {return address;}

    /**
     * Sets an object field to show the current position of a simpleTurnout object.
     * This position does not necessarily reflect the actual position of an associated
     * physical turnout.
     *
     * @param isclosed  true if the object is to be marked as closed.
     */
    public void setIsClosed(boolean isclosed) {
        isClosed = isclosed;
        isUnused = false;
    }

    /**
     * Returns position of the turnout as represented in an object field.  This
     * position does not necessarily reflect the actual position of an associated
     * physical turnout.
     *
     * @return true if turnout position is closed, else false
     */
    public boolean getIsClosed() {return isClosed;}

    /**
     * Returns the "used" state of the object, as represented in an object field.
     * This does not necessarily reflect the actual "used" state of the physical
     * device.
     *
     * @return true if turnout is "unused", else false
     */
    public boolean getIsUnused() {
        log.debug("simple turnout isunused returns {}", isUnused);
        return isUnused;
    }

    /**
     * Define the turnout as "unused" within a field in the object.
     */
    public void setIsUnused() {
        address = -1;
        isClosed = true;
        isUnused = true;
    }

    private final static Logger log = LoggerFactory.getLogger(SimpleTurnout.class);

}
