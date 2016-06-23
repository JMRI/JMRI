// PiSprogOneSerialDriverAdapter.java
package jmri.jmrix.sprog.pi.pisprogone;

import jmri.jmrix.sprog.SprogConstants.SprogMode;
import jmri.jmrix.sprog.update.SprogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the Sprog system.
 * <P>
 * This connects an SPROG DCC PI-SPROG One via a serial com port.
 * <P>
 * The current implementation only handles the 115,200 baud rate, and does not use
 * any other options at configuration time.
 *
 * @author	Andrew Crosland Copyright (C) 2016
 * @version	$Revision$
 */
public class PiSprogOneSerialDriverAdapter
        extends jmri.jmrix.sprog.serialdriver.SerialDriverAdapter {

    public PiSprogOneSerialDriverAdapter() {
        super(SprogMode.SERVICE, 115200, new SprogType(SprogType.PISPROGONE));
        this.getSystemConnectionMemo().setUserName("Pi-SPROG One Programmer");
    }

    /**
     * Get an array of valid baud rates. This is currently only 115,200 bps
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"115,200 bps"};
    }

    static public PiSprogOneSerialDriverAdapter instance() {
        if (mInstance == null) {
            PiSprogOneSerialDriverAdapter m = new PiSprogOneSerialDriverAdapter();
            m.setManufacturer(jmri.jmrix.sprog.SprogConnectionTypeList.SPROG);
            mInstance = m;
        }
        return mInstance;
    }
    static volatile PiSprogOneSerialDriverAdapter mInstance = null;

    static Logger log = LoggerFactory.getLogger(PiSprogOneSerialDriverAdapter.class.getName());

}

/* @(#)PiSprogOneSerialDriverAdapter.java */
