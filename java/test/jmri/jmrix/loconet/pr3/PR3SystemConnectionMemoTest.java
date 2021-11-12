package jmri.jmrix.loconet.pr3;

import jmri.jmrix.loconet.LnSystemConnectionMemoTestBase;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PR3SystemConnectionMemoTest extends LnSystemConnectionMemoTestBase<PR3SystemConnectionMemo> {

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        scm = new PR3SystemConnectionMemo();
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(scm);
        scm.setLnTrafficController(lnis);
        scm.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100, false, false, false, false);
        scm.configureManagers();
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.dispose();
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PR3SystemConnectionMemoTest.class);
}
