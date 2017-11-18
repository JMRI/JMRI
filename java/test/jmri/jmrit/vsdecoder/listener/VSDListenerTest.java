package jmri.jmrit.vsdecoder.listener;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of VSDListener
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class VSDListenerTest {

    @Test
    public void testCtor() {
        VSDListener s = new VSDListener();
        Assert.assertNotNull("exists", s);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
