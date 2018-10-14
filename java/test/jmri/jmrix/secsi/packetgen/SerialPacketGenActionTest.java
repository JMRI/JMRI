package jmri.jmrix.secsi.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;

/**
 * Test simple functioning of SerialPacketGenAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialPacketGenActionTest {

    private SecsiSystemConnectionMemo memo = null;

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SerialPacketGenAction action = new SerialPacketGenAction("secsi test Action",memo);
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SerialPacketGenAction action = new SerialPacketGenAction(memo); 
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new SecsiSystemConnectionMemo();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
