package jmri.jmrix.easydcc.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EasyDccPacketGenActionTest {

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EasyDccPacketGenAction t = new EasyDccPacketGenAction("SendPacket", new EasyDccSystemConnectionMemo("E", "EasyDCC Test"));
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EasyDccPacketGenAction t = new EasyDccPacketGenAction(new EasyDccSystemConnectionMemo("E", "EasyDCC Test"));
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EasyDccPacketGenActionTest.class);

}
