package jmri.jmrix.loconet.usb_dcs52;

import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LnSystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class UsbDcs52SystemConnectionMemoTest extends LnSystemConnectionMemoTestBase<UsbDcs52SystemConnectionMemo> {

    @Override
    @BeforeEach
    public void setUp() {
       super.setUp();
       scm = new UsbDcs52SystemConnectionMemo();
       LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(scm);
       scm.setLnTrafficController(lnis);
       scm.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100,false,false,false,false);
       scm.configureManagers();
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.dispose();
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UsbDcs52SystemConnectionMemoTest.class);

}
