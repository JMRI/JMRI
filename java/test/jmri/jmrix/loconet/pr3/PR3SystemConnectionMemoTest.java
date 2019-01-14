package jmri.jmrix.loconet.pr3;

import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PR3SystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
       JUnitUtil.setUp();
       PR3SystemConnectionMemo memo = new PR3SystemConnectionMemo();
       LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(memo);
       memo.setLnTrafficController(lnis);
       memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100,false,false,false);
       memo.configureManagers();
       scm = memo;
    }

    @Override
    @After
    public void tearDown() {
        ((PR3SystemConnectionMemo)scm).dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PR3SystemConnectionMemoTest.class);

}
