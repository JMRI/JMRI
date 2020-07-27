package jmri.jmrix.sprog.pi.pisprogone;

import jmri.jmrix.sprog.SprogConstants.SprogMode;
import jmri.jmrix.sprog.update.SprogType;

/**
 * Implements SerialPortAdapter for the Sprog system.
 * <p>
 * This connects an SPROG DCC PI-SPROG One via a serial com port.
 * <p>
 * The current implementation only handles the 115,200 baud rate, and does not use
 * any other options at configuration time.
 *
 * @author Andrew Crosland Copyright (C) 2016
 */
public class PiSprogOneSerialDriverAdapter
        extends jmri.jmrix.sprog.serialdriver.SerialDriverAdapter {

    public PiSprogOneSerialDriverAdapter() {
        super(SprogMode.SERVICE, 115200, new SprogType(SprogType.PISPROGONE));
        this.getSystemConnectionMemo().setUserName(Bundle.getMessage("PiSprog1ProgrammerTitle"));
    }

    /**
     * {@inheritDoc}
     * Currently only 115,200 bps
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"115,200 bps"};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{115200};
    }
    
    // private final static Logger log = LoggerFactory.getLogger(PiSprogOneSerialDriverAdapter.class);

}
