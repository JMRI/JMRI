package jmri.jmrix.zimo.swing.monitor;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of Mx1MonPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class Mx1MonPanelTest {

    @Test
    public void testMemoCtor() {
        Mx1MonPanel action = new Mx1MonPanel();
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
