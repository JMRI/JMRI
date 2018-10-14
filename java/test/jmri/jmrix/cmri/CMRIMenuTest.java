package jmri.jmrix.cmri;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CMRIMenu
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class CMRIMenuTest {


    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        CMRIMenu action = new CMRIMenu(new CMRISystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        CMRIMenu action = new CMRIMenu("CMRI Test Menu", new CMRISystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
