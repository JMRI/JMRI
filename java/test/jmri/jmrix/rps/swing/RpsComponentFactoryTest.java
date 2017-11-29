package jmri.jmrix.rps.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.rps.RpsSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of RpsComponentFactory
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class RpsComponentFactoryTest {


    private RpsSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        RpsComponentFactory action = new RpsComponentFactory(m);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = new RpsSystemConnectionMemo();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
