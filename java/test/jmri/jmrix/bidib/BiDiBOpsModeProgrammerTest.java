package jmri.jmrix.bidib;

import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;

import jmri.util.JUnitUtil;

/**
 * Tests for the BiDiBOpsModeProgrammer class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {
    
    private BiDiBSystemConnectionMemo memo;
    
    @Override
    @Test
    public void testGetCanRead() {
        // BiDiB supports railcom!
        Assertions.assertTrue( programmer.getCanRead(), "can read");
    }

    @Override
    @Test
    @Disabled("Test requires further development")
    public void testGetCanReadAddress() {
        //Assert.assertFalse("can read address", programmer.getCanRead("1234"));
    }

    @Override
    @Test
    @Disabled("Test requires further development")
    public void testGetCanWriteAddress() {
        //Assert.assertTrue("can write address", programmer.getCanWrite("1234"));
    }

    @Test
    @Override
    public void testWriteCVNullListener() throws jmri.ProgrammerException {
        super.testWriteCVNullListener();
        // test may require further setup?
        //JUnitAppender.suppressWarnMessageStartsWith(
        //    "The node is no longer registered. Skip send message to node:");
        JUnitAppender.suppressErrorMessageStartsWith(
            "writePom async failed on node:"); //since jbidibc 2.0.18
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        memo = new BiDiBSystemConnectionMemo();
        memo.setBiDiBTrafficController(new TestBiDiBTrafficController(new BiDiBInterfaceScaffold()));
        programmer = new BiDiBOpsModeProgrammer(5, memo.getBiDiBTrafficController());
    }
    
    @Override
    @AfterEach
    public void tearDown() {
        programmer = null;
        memo.dispose();
        memo.getBiDiBTrafficController().terminate();
        //JUnitUtil.resetWindows(false,false);
        //JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
