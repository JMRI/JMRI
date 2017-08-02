package jmri.jmrix.roco.z21;

import org.junit.After;
import org.junit.Before;
import jmri.jmrix.lenz.LenzCommandStation;

/**
 * <p>
 * Title: Z21PacketizerTest </p>
 * <p>
 *
 * @author Bob Jacobsen Copyrgiht (C) 2002
 * @author Paul Bender Copyright (C) 2016
 */
public class Z21XNetPacketizerTest extends jmri.jmrix.lenz.XNetPacketizerTest {

    // The minimal setup for log4J
    @Before
    @Override 
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        LenzCommandStation lcs = new LenzCommandStation();
        tc = new Z21XNetPacketizer(lcs) {
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }
        };
    }

    @After
    @Override
    public void tearDown() {
        tc=null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
