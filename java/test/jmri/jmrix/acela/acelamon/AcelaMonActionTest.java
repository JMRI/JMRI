package jmri.jmrix.acela.acelamon;

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
 * Test simple functioning of AcelaMonAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class AcelaMonActionTest {

    @Test
    public void testStringMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AcelaMonAction action = new AcelaMonAction("Acela test Action", new AcelaSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testDefaultCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AcelaSystemConnectionMemo memo = new AcelaSystemConnectionMemo();
        InstanceManager.setDefault(AcelaSystemConnectionMemo.class, memo);
        AcelaMonAction action = new AcelaMonAction();
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
