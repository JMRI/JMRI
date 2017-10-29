package jmri.jmrix.dcc4pc;

import jmri.progdebugger.DebugProgrammerManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Dcc4PcProgrammerManagerTest {

    @Test
    public void testCTor() {
        Dcc4PcProgrammerManager t = new Dcc4PcProgrammerManager(new DebugProgrammerManager());
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Dcc4PcProgrammerManagerTest.class);

}
