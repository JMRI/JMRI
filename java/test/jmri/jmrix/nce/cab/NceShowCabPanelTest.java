package jmri.jmrix.nce.cab;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NceShowCabPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NceShowCabPanelTest {

    @Test
    public void testCtor() {
        NceShowCabPanel action = new NceShowCabPanel();
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
