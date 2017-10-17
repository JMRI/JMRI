package jmri.jmrix.jinput.treecontrol;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
