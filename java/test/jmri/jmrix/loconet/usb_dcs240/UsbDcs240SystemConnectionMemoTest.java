package jmri.jmrix.loconet.usb_dcs240;

import jmri.jmrix.loconet.LnSystemConnectionMemoTestBase;
import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class UsbDcs240SystemConnectionMemoTest extends LnSystemConnectionMemoTestBase<UsbDcs240SystemConnectionMemo> {

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        scm = new UsbDcs240SystemConnectionMemo();
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(scm);
        scm.setLnTrafficController(lnis);
        scm.configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100, false, false, false, false);
        scm.configureManagers();
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.dispose();
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SBDCS240SystemConnectionMemoTest.class);
}
