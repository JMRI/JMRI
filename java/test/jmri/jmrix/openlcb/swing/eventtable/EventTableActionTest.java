package jmri.jmrix.openlcb.swing.eventtable;

import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Bob Jacobsen  (c) 2026
 */
public class EventTableActionTest {

    private TrafficControllerScaffold tcs = null; 
    private CanSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        var t = new EventTableAction();
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initConfigureManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        tcs = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcs);
        InstanceManager.setDefault(CanSystemConnectionMemo.class,memo);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        memo = null;
        tcs.terminateThreads();
        tcs = null;
        JUnitUtil.tearDown();

    }

    // private static final Logger log = LoggerFactory.getLogger(EventTableActionTest.class);

}
