package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.LenzCommandStation;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

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
        JUnitUtil.setUp();
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
        JUnitUtil.tearDown();
    }

}
