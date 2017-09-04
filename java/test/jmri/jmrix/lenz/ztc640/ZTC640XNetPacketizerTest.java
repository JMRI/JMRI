package jmri.jmrix.lenz.ztc640;

import org.junit.Before;


/**
 * <p>
 * Title: ZTC640XNetPacketizerTest </p>
 * <p>
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class ZTC640XNetPacketizerTest extends jmri.jmrix.lenz.XNetPacketizerTest {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        tc = new ZTC640XNetPacketizer(new jmri.jmrix.lenz.LenzCommandStation()) {
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }
        };
    }

}
