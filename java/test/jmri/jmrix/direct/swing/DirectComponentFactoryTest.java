package jmri.jmrix.direct.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.direct.DirectSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of DirectComponentFactory
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class DirectComponentFactoryTest {

    private DirectSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        DirectComponentFactory action = new DirectComponentFactory(m);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = new DirectSystemConnectionMemo();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown(); 
    }
}
