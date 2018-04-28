package jmri.jmrix.loconet.uhlenbrock;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.UhlenbrockSlotManager;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class UhlenbrockSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
       JUnitUtil.setUp();
       LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
       UhlenbrockSystemConnectionMemo memo = new UhlenbrockSystemConnectionMemo();
       memo.setLnTrafficController(lnis);
       memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_IBX_TYPE_2,false,false);
       memo.configureManagers();
       scm = memo;
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UhlenbrockSystemConnectionMemoTest.class);

}
