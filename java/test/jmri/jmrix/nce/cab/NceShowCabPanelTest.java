package jmri.jmrix.nce.cab;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
