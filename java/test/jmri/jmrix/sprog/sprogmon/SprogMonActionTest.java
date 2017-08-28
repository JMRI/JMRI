package jmri.jmrix.sprog.sprogmon;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of SprogMonAction  
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SprogMonActionTest {


    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        SprogMonAction action = new SprogMonAction("SPROG Action Test",new jmri.jmrix.sprog.SprogSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
