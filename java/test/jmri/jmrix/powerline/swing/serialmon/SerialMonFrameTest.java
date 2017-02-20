package jmri.jmrix.powerline.swing.serialmon;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.jmrix.powerline.SerialTrafficControlScaffold;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of SerialMonFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialMonFrameTest {


    private SerialTrafficControlScaffold tc = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        SerialMonFrame action = new SerialMonFrame(tc);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        tc = new SerialTrafficControlScaffold();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
        tc = null;
    }
}
