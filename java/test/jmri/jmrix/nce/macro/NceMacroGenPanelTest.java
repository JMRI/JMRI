package jmri.jmrix.nce.macro;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NceMacroGenPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NceMacroGenPanelTest {

    @Test
    public void testCtor() {
        NceMacroGenPanel action = new NceMacroGenPanel();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
