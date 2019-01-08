package jmri.jmrit.cabsignals;

import java.awt.GraphicsEnvironment;
import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CabSignalAction
 *
 * @author	Paul Bender Copyright (C) 2019
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
       JUnitUtil.tearDown();    
    }
}
