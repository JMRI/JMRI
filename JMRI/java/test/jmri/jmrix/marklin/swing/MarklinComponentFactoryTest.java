package jmri.jmrix.marklin.swing;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of MarklinComponentFactory
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class MarklinComponentFactoryTest {


    private jmri.jmrix.marklin.MarklinSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        MarklinComponentFactory action = new MarklinComponentFactory(m);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = new jmri.jmrix.marklin.MarklinSystemConnectionMemo();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
