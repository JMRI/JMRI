package jmri.jmrix.loconet;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the LocoNetSystemConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LocoNetSystemConnectionMemoTest extends SystemConnectionMemoTestBase<LocoNetSystemConnectionMemo> {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        scm = new LocoNetSystemConnectionMemo();
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(scm);
        scm.setLnTrafficController(lnis);
        scm.configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100, false, false, false);
        scm.configureManagers();
    }

    @Override
    @After
    public void tearDown() {
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
