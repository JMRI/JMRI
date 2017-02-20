package jmri.jmrix.xpa.swing.packetgen;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * @author Paul Bender Copyright(C) 2016
 */
public class XpaPacketGenActionTest {

    private jmri.jmrix.xpa.XpaSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("XpaPacketGenAction exists",new XpaPacketGenAction("Test",memo) );
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();

        memo = new jmri.jmrix.xpa.XpaSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.xpa.XpaSystemConnectionMemo.class,memo);

    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
