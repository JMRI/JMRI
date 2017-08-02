package jmri.jmrix.roco.z21;

import org.junit.After;
import org.junit.Before;

/**
 * Tests for the jmri.jmrix.roco.z21.z21TrafficController class
 *
 * @author	Paul Bender
 */
public class Z21TrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tc = new Z21TrafficController();
    }
    
    @Override
    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
