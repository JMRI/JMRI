package jmri.jmrix.powerline.swing.serialmon;

import jmri.jmrix.powerline.SerialTrafficControlScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of SerialMonPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {


    private SerialTrafficControlScaffold tc = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists",pane);
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new SerialTrafficControlScaffold();
        pane = new SerialMonPane();
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();        tc = null;
    }
}
