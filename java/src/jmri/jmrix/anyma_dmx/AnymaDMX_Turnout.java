package jmri.jmrix.anyma_dmx;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turnout interface to AnymaDMX_DMX
 * <P>
 *
 * @author Paul Bender Copyright (C) 2015
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class AnymaDMX_Turnout extends AbstractTurnout implements Turnout, java.io.Serializable {

    // in theory dmx can be static, because there will only ever
    // be one, but the library handles the details that make it a
    // singleton.
    private DMX_Controller dmx = null;
//    private DmxPinDigitalOutput pin = null;
    private int address;

    public AnymaDMX_Turnout(String systemName) {
        this(systemName, DMX_Factory.getInstance());
    }

    public AnymaDMX_Turnout(String systemName, String userName) {
        this(systemName, userName, DMX_Factory.getInstance());
    }

    public AnymaDMX_Turnout(String systemName, DMX_Controller _dmx) {
        super(systemName.toUpperCase());
        log.debug("*	Provisioning turnout {}", systemName);
        init(systemName.toUpperCase(), _dmx);
    }

    public AnymaDMX_Turnout(String systemName, String userName, DMX_Controller _dmx) {
        super(systemName.toUpperCase(), userName);
        log.debug("*	Provisioning turnout {} with username '{}'", systemName, userName);
        init(systemName.toUpperCase(), _dmx);
    }

    /**
     * Common initilization for all constructors
     */
    private void init(String systemName, DMX_Controller _dmx) {
        dmx = _dmx;
        address = Integer.parseInt(getSystemName().substring(getSystemName().lastIndexOf("T") + 1));
        //pin = dmx.provisionDigitalOutputPin(RaspiPin.getPinByName("DMX " + address), getSystemName());
        //pin.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
    }

    //support inversion for RPi turnouts
    @Override
    public boolean canInvert() {
        return true;
    }

    /**
     * Handle a request to change state, typically by sending a message to the
     * layout in some child class. Public version (used by TurnoutOperator)
     * sends the current commanded state without changing it.
     *
     * @param s new state value
     */
    @Override
    protected void forwardCommandChangeToLayout(int s) {
        if (s == CLOSED) {
            log.debug("*	Setting turnout {} to CLOSED", getSystemName());
            if (!getInverted()) {
                //pin.high();
            } else {
                //pin.low();
            }
        } else if (s == THROWN) {
            log.debug("*	Setting turnout {} to THROWN", getSystemName());
            if (!getInverted()) {
                //pin.low();
            } else {
                //pin.high();
            }
        }
    }

    @Override
    protected void turnoutPushbuttonLockout(boolean locked) {
    }

    private final static Logger log = LoggerFactory.getLogger(AnymaDMX_Turnout.class);
}
