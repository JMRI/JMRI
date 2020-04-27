package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Tests for the jmri.jmrix.roco.z21.z21TrafficController class
 *
 * @author Paul Bender
 */
public class Z21TrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new Z21TrafficController();
    }

    @Override
    @After
    public void tearDown() {
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }

}
