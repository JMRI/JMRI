package jmri.jmrix.loconet;

import jmri.jmrix.loconet.LnSystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the LocoNetSystemConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LocoNetSystemConnectionMemoTest extends LnSystemConnectionMemoTestBase<LocoNetSystemConnectionMemo> {

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        scm = new LocoNetSystemConnectionMemo();
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(scm);
        scm.setLnTrafficController(lnis);
        scm.configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100, false, false, false);
        scm.configureManagers();
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.dispose();
        super.tearDown();
    }

}
