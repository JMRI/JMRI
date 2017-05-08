package jmri.jmrix.dcc4pc;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(Dcc4PcOpsModeProgrammerTest.class.getName());

}
