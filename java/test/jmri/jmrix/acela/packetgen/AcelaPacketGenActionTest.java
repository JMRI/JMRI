package jmri.jmrix.acela.packetgen;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.InstanceManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of AcelaPacketGenAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class AcelaPacketGenActionTest {

    @Test
    public void testStringMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AcelaPacketGenAction action = new AcelaPacketGenAction("Acela test Action", new AcelaSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testDefaultCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AcelaSystemConnectionMemo memo = new AcelaSystemConnectionMemo();
        InstanceManager.setDefault(AcelaSystemConnectionMemo.class, memo);
        AcelaPacketGenAction action = new AcelaPacketGenAction();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
