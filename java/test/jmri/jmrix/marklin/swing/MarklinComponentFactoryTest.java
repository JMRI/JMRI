package jmri.jmrix.marklin.swing;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

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
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        m = new jmri.jmrix.marklin.MarklinSystemConnectionMemo();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
