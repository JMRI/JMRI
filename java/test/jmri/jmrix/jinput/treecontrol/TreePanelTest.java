package jmri.jmrix.jinput.treecontrol;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test simple functioning of TreePanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class TreePanelTest {

    @Test
    @Ignore("travis and appveyor have trouble loading the proper library for this test")
    public void testCtor() {
        TreePanel action = new TreePanel();
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
