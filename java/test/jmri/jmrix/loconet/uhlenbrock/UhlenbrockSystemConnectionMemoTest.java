package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.loconet.LocoNetInterfaceScaffold;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class UhlenbrockSystemConnectionMemoTest extends SystemConnectionMemoTestBase<UhlenbrockSystemConnectionMemo> {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new UhlenbrockSystemConnectionMemo();
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(scm);
        scm.setLnTrafficController(lnis);
        scm.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_IBX_TYPE_2, false, false, false);
        scm.configureManagers();
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UhlenbrockSystemConnectionMemoTest.class);
}
