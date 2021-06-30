package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import jmri.jmrix.can.ConfigurationManager;

/**
 * Implements SerialPortAdapter for SPROG Generation 5 Pi-SPROG 3 v2.
 * <p>
 * This connects a SPROG Generation 5 Pi-SPROG 3 v2 via a serial com port (real or virtual).
 * Normally controlled by the SerialDriverFrame class.
 * 
 * This hardware has only one track output so can be in either programmer or command
 * station mode, not both.
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2009
 * @author Andrew Crosland Copyright (C) 2019, 2021
 */
public class PiSprog3v2SerialDriverAdapter extends Sprog3PlusSerialDriverAdapter {

    public PiSprog3v2SerialDriverAdapter() {
        super();
        _progMode = ConfigurationManager.ProgModeSwitch.EITHER;
    }

}
