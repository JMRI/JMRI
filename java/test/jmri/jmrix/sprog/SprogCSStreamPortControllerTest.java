package jmri.jmrix.sprog;

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
 * Tests for the jmri.jmrix.sprog.SprogCSStreamPortController class.
 *
 * @author	Paul Bender Copyright (C) 2014-2016
 */
public class SprogCSStreamPortControllerTest extends jmri.jmrix.AbstractStreamPortControllerTestBase {

    @Test
    public void testCtor() {
       Assert.assertNotNull("exists", apc);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        try {
            PipedInputStream tempPipe;
            tempPipe = new PipedInputStream();
            DataOutputStream ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
            tempPipe = new PipedInputStream();
            DataInputStream istream = new DataInputStream(tempPipe);

            apc = new SprogCSStreamPortController(istream, ostream, "Test");
        } catch (java.io.IOException ioe) {
            Assert.fail("IOException creating stream");
        }
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
