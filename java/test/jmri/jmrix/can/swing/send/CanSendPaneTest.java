package jmri.jmrix.can.swing.send;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
