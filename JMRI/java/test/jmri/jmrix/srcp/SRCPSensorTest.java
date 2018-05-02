package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * SRCPSensorTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPSensor class
 *
 * @author	Bob Jacobsen
 */
public class SRCPSensorTest {
        
    private SRCPSensor s = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull(s);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        SRCPBusConnectionMemo sm = new SRCPBusConnectionMemo(new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            }
        }, "A", 1);
        s = new SRCPSensor(1, sm);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
