package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * DCCppProgrammerManagerTest.java
 *
 * Test for the jmri.jmrix.dccpp.DCCppProgrammerManager class
 *
 * @author Paul Bender
 * @author Mark Underwood (C) 2015
 */
public class DCCppProgrammerManagerTest {

    @Test
    public void testCtor() {
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppProgrammerManager t = new DCCppProgrammerManager(new DCCppProgrammer(tc), new DCCppSystemConnectionMemo(tc));
        Assert.assertNotNull(t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
