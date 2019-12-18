package jmri.jmrix.tams;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * TamsTrafficControllerTest.java
 *
 * Description:	tests for the jmri.jmrix.tams.TamsTrafficController
 * class
 *
 * @author	Bob Jacobsen
 */
public class TamsTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new TamsTrafficController();
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
