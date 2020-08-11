package jmri.jmrit.cabsignals;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 * Test simple functioning of CabSignalAction
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class CabSignalActionTest {

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CabSignalAction action = new CabSignalAction("Test CabSignal Tool Action");
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CabSignalAction action = new CabSignalAction();
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
       JUnitUtil.tearDown();    
    }
}
