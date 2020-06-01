package jmri.jmrix.loconet.hexfile;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class HexFileSystemConnectionMemoTest extends SystemConnectionMemoTestBase<HexFileSystemConnectionMemo> {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        scm = new HexFileSystemConnectionMemo();
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(scm);
        scm.setLnTrafficController(lnis);
        scm.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100, false, false, false);
        scm.configureManagers();
    }

    @Override
    @After
    public void tearDown() {
        scm.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocoNetSystemConnectionMemoTest.class);
}
