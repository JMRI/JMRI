package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * DCCppProgrammerManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppProgrammerManager class
 *
 * @author	Paul Bender
 * @author	Mark Underwood (C) 2015
 */
public class DCCppProgrammerManagerTest {

    @Test
    public void testCtor() {
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppProgrammerManager t = new DCCppProgrammerManager(new DCCppProgrammer(tc), new DCCppSystemConnectionMemo(tc));
        Assert.assertNotNull(t);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
