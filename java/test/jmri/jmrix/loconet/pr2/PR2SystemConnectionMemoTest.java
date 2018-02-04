package jmri.jmrix.loconet.pr2;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PR2SystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
       JUnitUtil.setUp();
       LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
       PR2SystemConnectionMemo memo = new PR2SystemConnectionMemo();
       memo.setLnTrafficController(lnis);
       memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100,false,false);
       memo.configureManagers();
       scm = memo;
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PR2SystemConnectionMemoTest.class);

}
