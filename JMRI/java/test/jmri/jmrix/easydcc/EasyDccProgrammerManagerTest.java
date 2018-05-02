package jmri.jmrix.easydcc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EasyDccProgrammerManagerTest {


    private EasyDccTrafficControlScaffold tc = null;
    private EasyDccSystemConnectionMemo memo = null;


    @Test
    public void testCTor() {
        // infrastructure objects
        EasyDccProgrammer p = new EasyDccProgrammer(memo);
        EasyDccProgrammerManager t = new EasyDccProgrammerManager(p, memo);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new EasyDccTrafficControlScaffold(null);
        memo = new EasyDccSystemConnectionMemo(tc);
    }

    @After
    public void tearDown() {
        tc.terminateThreads();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EasyDccProgrammerManagerTest.class);

}
