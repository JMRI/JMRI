package jmri.jmrix.sprog.console;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of SprogConsoleAction.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogConsoleActionTest {


    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        SprogConsoleAction action = new SprogConsoleAction("SPROG Action Test", new jmri.jmrix.sprog.SprogSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }

}
