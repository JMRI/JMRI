package jmri.jmrix.sprog.swing;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of SprogComponentFactory
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogComponentFactoryTest {


    private jmri.jmrix.sprog.SprogSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        SprogComponentFactory action = new SprogComponentFactory(m);
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new jmri.jmrix.sprog.SprogSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
