package jmri.jmrix.tams.swing.statusframe;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of StatusPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class StatusPanelTest {


    @Test
    public void testCtor() {
        StatusPanel action = new StatusPanel();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
