package jmri.jmrix.marklin.swing.monitor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of MarklinMonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class MarklinMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {


    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", pane );
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        pane = new MarklinMonPane();
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
