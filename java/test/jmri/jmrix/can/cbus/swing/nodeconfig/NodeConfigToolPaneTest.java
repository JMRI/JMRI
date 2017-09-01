package jmri.jmrix.can.cbus.swing.nodeconfig;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NodeConfigToolPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NodeConfigToolPaneTest {

    @Test
    public void testCtor() {
        NodeConfigToolPane pane = new NodeConfigToolPane();
        Assert.assertNotNull("exists", pane);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
