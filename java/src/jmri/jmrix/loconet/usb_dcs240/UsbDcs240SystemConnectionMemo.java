package jmri.jmrix.loconet.usb_dcs240;

import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.ThrottleManager;
import jmri.implementation.QuietShutDownTask;
import jmri.jmrix.loconet.LnPowerManager;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.pr3.PR3SystemConnectionMemo;
import jmri.jmrix.loconet.SlotManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a DCS240 USB interface is active
 * <p>
 * This class serves as a placeholder; it is anticipated that the DCS240
 * implementation will require differentiation from the PR3 implementation.
 * <p>
 * Based on PR3SystemConnectionMemo
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author B. Milhaupt Copyright (C) 2017, 2019
 */
public class UsbDcs240SystemConnectionMemo extends PR3SystemConnectionMemo {

//    private final static Logger log = LoggerFactory.getLogger(UsbDcs240SystemConnectionMemo.class);

}
