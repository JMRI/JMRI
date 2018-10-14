package jmri.jmrix.tams.swing;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of TamsComponentFactory
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class TamsComponentFactoryTest {


    private jmri.jmrix.tams.TamsSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        TamsComponentFactory action = new TamsComponentFactory(m);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = new jmri.jmrix.tams.TamsSystemConnectionMemo();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
