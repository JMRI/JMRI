package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * DCCppOpsModeProgrammerTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppOpsModeProgrammer class
 *
 * @author	Paul Bender
 * @author	Mark Underwood (C) 2015
 */
public class DCCppOpsModeProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {
 
    @Test
    public void testCtor() {
        Assert.assertNotNull(abstractprogrammer);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppOpsModeProgrammer t = new DCCppOpsModeProgrammer(5, tc);
	abstractprogrammer = t;
    }

    @Override
    @After
    public void tearDown() {
	abstractprogrammer = null;
        JUnitUtil.tearDown();
    }

}
