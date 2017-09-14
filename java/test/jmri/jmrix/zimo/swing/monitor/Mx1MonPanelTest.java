package jmri.jmrix.zimo.swing.monitor;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
