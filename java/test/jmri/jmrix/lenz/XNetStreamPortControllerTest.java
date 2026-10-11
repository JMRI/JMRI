package jmri.jmrix.lenz;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * XNetStreamPortControllerTest.java
 *
 * Test for the jmri.jmrix.lenz.XNetStreamPortController class
 *
 * @author Paul Bender
 */
public class XNetStreamPortControllerTest extends jmri.jmrix.AbstractStreamPortControllerTestBase {

    @Test
    public void testCtor() {
        Assertions.assertNotNull( apc, "exists");
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        apc = Assertions.assertDoesNotThrow( () -> {
            PipedInputStream tempPipe;
            tempPipe = new PipedInputStream();
            DataOutputStream ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
            tempPipe = new PipedInputStream();
            DataInputStream istream = new DataInputStream(tempPipe);
            return new XNetStreamPortController(istream, ostream, "Test");
        }, "Exception creating stream");

    }

    @Override
    @AfterEach
    public  void tearDown() {
        apc = null;
        JUnitUtil.tearDown();
    }

}
