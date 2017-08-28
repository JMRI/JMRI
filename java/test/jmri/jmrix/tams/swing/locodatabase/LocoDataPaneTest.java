package jmri.jmrix.tams.swing.locodatabase;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of LocoDataPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LocoDataPaneTest {


    @Test
    public void testCtor() {
        LocoDataPane action = new LocoDataPane();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
