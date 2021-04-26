package jmri.jmrix.marklin.swing;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of MarklinComponentFactory
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MarklinComponentFactoryTest {


    private jmri.jmrix.marklin.MarklinSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        MarklinComponentFactory action = new MarklinComponentFactory(m);
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new jmri.jmrix.marklin.MarklinSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
