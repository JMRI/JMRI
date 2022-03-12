package jmri.jmrix.marklin.swing;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of MarklinMenu
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MarklinMenuTest {


    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        MarklinMenu action = new MarklinMenu(new jmri.jmrix.marklin.MarklinSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
