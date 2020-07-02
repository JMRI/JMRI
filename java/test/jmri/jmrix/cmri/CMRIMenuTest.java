package jmri.jmrix.cmri;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of CMRIMenu
 *
 * @author Paul Bender Copyright (C) 2016
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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
