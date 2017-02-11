package jmri.jmrix.tams.swing.monitor;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of TamsMonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class TamsMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {


    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", pane );
    }

    @Override
    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        pane = new TamsMonPane();
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
