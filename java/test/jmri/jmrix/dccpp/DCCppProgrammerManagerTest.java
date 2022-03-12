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
        DCCppProgrammer dccprogrammer = new DCCppProgrammer(tc);

        DCCppProgrammerManager t = new DCCppProgrammerManager(dccprogrammer, new DCCppSystemConnectionMemo(tc));
        Assertions.assertNotNull(t, "exists");

        dccprogrammer.dispose();
        tc.terminateThreads();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
