package jmri.jmrix.srcp.swing;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.srcp.SRCPSystemConnectionMemo;
import jmri.jmrix.srcp.SRCPTrafficController;
import jmri.jmrix.srcp.SRCPMessage;
import jmri.jmrix.srcp.SRCPListener;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SRCPComponentFactoryTest {

    private SRCPSystemConnectionMemo m = null;

    @Test
    public void testCTor() {
        SRCPComponentFactory t = new SRCPComponentFactory(m);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        SRCPTrafficController et = new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener l) {
                // we aren't actually sending anything to a layout.
            }
        };
        m = new SRCPSystemConnectionMemo(et);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SRCPComponentFactoryTest.class.getName());

}
