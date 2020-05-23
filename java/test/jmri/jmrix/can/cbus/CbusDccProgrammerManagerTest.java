package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CbusDccProgrammerManagerTest {

    @Test
    public void testCTor() {
        CbusDccProgrammerManager t = new CbusDccProgrammerManager(new CbusDccProgrammer(tc),memo);
        Assert.assertNotNull("exists",t);
    }
    
    private TrafficControllerScaffold tc;
    private CanSystemConnectionMemo memo;
    private CbusPreferences prefs;

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        prefs = new CbusPreferences();
        jmri.InstanceManager.store(prefs,CbusPreferences.class );
    }

    @After
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusDccProgrammerManagerTest.class);

}
