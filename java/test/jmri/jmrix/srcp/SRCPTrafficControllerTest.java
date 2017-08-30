package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * SRCPTrafficControllerTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPTrafficController class
 *
 * @author	Bob Jacobsen
 */
public class SRCPTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        tc = new SRCPTrafficController();
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
