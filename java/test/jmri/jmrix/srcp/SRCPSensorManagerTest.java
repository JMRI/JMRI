package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * SRCPSensorManagerTest.java
 * <p>
 * Description:	tests for the jmri.jmrix.srcp.SRCPSensorManager class
 *
 * @author	Bob Jacobsen
 * @author Paul Bender Copyright (C) 2016
 */
public class SRCPSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "A1S" + i;
    }

    public void testCtor() {
        Assert.assertNotNull(l);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        SRCPBusConnectionMemo sm = new SRCPBusConnectionMemo(new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            }
        }, "A", 1);

        l = new SRCPSensorManager(sm, 1);
    }

    @After
    public void tearDown() {
        l.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
