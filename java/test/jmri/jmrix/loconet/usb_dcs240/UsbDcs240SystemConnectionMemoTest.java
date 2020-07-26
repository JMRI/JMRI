package jmri.jmrix.loconet.usb_dcs240;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class UsbDcs240SystemConnectionMemoTest extends SystemConnectionMemoTestBase<UsbDcs240SystemConnectionMemo> {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new UsbDcs240SystemConnectionMemo();
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(scm);
        scm.setLnTrafficController(lnis);
        scm.configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100, false, false, false);
        scm.configureManagers();
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SBDCS240SystemConnectionMemoTest.class);
}
