package jmri.jmrix.dcc4pc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Dcc4PcOpsModeProgrammerTest {

    @Test
    public void testCTor() {
        Dcc4PcProgrammerManager pm = new Dcc4PcProgrammerManager(jmri.InstanceManager.getDefault(jmri.ProgrammerManager.class));
        Dcc4PcOpsModeProgrammer t = new Dcc4PcOpsModeProgrammer(false,5,pm);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Dcc4PcOpsModeProgrammerTest.class.getName());

}
