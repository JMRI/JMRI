package jmri.jmrix.rps.swing;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.rps.RpsSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of RpsComponentFactory
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class RpsComponentFactoryTest {


    private RpsSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        RpsComponentFactory action = new RpsComponentFactory(m);
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new RpsSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
