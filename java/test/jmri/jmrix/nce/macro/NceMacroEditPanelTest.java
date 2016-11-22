package jmri.jmrix.nce.macro;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.InstanceManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NceMacroEditPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NceMacroEditPanelTest {

    @Test
    public void testCtor() {
        NceMacroEditPanel action = new NceMacroEditPanel();
        Assert.assertNotNull("exists", action);
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
