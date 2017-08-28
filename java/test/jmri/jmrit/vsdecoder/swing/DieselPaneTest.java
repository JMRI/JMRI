package jmri.jmrit.vsdecoder.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of DieselPane
 *
 * @author	Paul Bender Copyright (C) 2015,2016
 */
public class DieselPaneTest {

    @Test
    public void testCtor() {
        DieselPane frame = new DieselPane();
        Assert.assertNotNull("exists", frame );
    }

    @Test
    public void testStringCtor() {
        DieselPane frame = new DieselPane("test pane");
        Assert.assertNotNull("exists", frame );
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
