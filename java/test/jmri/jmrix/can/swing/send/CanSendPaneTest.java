package jmri.jmrix.can.swing.send;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CanSendPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CanSendPaneTest {

    @Test
    public void testCtor() {
        CanSendPane pane = new CanSendPane();
        Assert.assertNotNull("exists", pane);
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
