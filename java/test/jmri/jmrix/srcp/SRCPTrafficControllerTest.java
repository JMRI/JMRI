package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * SRCPTrafficControllerTest.java
 *
 * Test for the jmri.jmrix.srcp.SRCPTrafficController class
 *
 * @author Bob Jacobsen
 */
public class SRCPTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new SRCPTrafficController();
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
