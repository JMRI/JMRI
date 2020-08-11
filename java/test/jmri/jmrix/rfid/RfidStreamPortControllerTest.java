package jmri.jmrix.rfid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;



/**
 * RfidStreamPortControllerTest.java
 *
 * Test for the jmri.jmrix.rfid.RfidStreamPortController class
 *
 * @author Paul Bender
 */
public class RfidStreamPortControllerTest extends jmri.jmrix.AbstractStreamPortControllerTestBase {

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", apc);
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        try {
            PipedInputStream tempPipe;
            tempPipe = new PipedInputStream();
            DataOutputStream ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
            tempPipe = new PipedInputStream();
            DataInputStream istream = new DataInputStream(tempPipe);

            apc = new RfidStreamPortController(istream, ostream, "Test");
        } catch (java.io.IOException ioe) {
            Assert.fail("IOException creating stream");
        }
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
