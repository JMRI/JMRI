package jmri.jmrix.direct.swing;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.direct.DirectSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of DirectComponentFactory
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class DirectComponentFactoryTest {

    private DirectSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        DirectComponentFactory action = new DirectComponentFactory(m);
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new DirectSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown(); 
    }
}
