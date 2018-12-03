package jmri.jmrix.lenz.ztc640;

import jmri.jmrix.lenz.XNetPortControllerScaffold;
import org.junit.Before;
import org.junit.Assert;


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
        jmri.util.JUnitUtil.setUp();
        tc = new ZTC640XNetPacketizer(new jmri.jmrix.lenz.LenzCommandStation()) {
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }
        };
        try {
           port = new XNetPortControllerScaffold();
        } catch (Exception e) {
           Assert.fail("Error creating test port");
        }
    }

}
