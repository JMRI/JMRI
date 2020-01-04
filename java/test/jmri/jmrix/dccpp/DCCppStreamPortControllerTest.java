package jmri.jmrix.dccpp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DCCppStreamPortControllerTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppStreamPortController class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 * @author	Mark Underwood (C) 2015
 */
public class DCCppStreamPortControllerTest extends jmri.jmrix.AbstractStreamPortControllerTestBase {

    @Test
    public void testCtor() {
       Assert.assertNotNull("exists", apc);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        try {
            PipedInputStream tempPipe;
            tempPipe = new PipedInputStream();
            DataOutputStream ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
            tempPipe = new PipedInputStream();
            DataInputStream istream = new DataInputStream(tempPipe);
            apc = new DCCppStreamPortController(istream, ostream, "Test");
        } catch (java.io.IOException ioe) {
            Assert.fail("IOException creating stream");
        }
    }

    @Override
    @After 
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

}
