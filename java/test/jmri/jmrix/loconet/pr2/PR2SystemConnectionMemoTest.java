package jmri.jmrix.loconet.pr2;

<<<<<<< HEAD
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
=======
import jmri.jmrix.SystemConnectionMemoTestBase;
>>>>>>> master
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PR2SystemConnectionMemoTest extends SystemConnectionMemoTestBase<PR2SystemConnectionMemo> {

    @Override
    @Before
    public void setUp() {
       JUnitUtil.setUp();
       scm = new PR2SystemConnectionMemo();
       LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(scm);
       scm.setLnTrafficController(lnis);
       scm.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100,false,false,false,false);
       scm.configureManagers();
    }

    @Override
    @After
    public void tearDown() {
        scm.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PR2SystemConnectionMemoTest.class);

}
