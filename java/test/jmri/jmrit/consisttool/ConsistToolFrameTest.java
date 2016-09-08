package jmri.jmrit.consisttool;

import apps.tests.Log4JFixture;
import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of ConsistToolFrame
 *
 * @author	Paul Bender Copyright (C) 2015,2016
 */
public class ConsistToolFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConsistToolFrame frame = new ConsistToolFrame();
        Assert.assertNotNull("exists", frame );
    }

    @Before
    public void setUp() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.setUp();
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }


}
