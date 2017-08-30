package jmri.jmrit.consisttool;

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
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testCtorWithCSpossible() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // overwrite the default consist manager set in setUp for this test
        // so that we can check initilization with CSConsists possible.
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager(){
             @Override
             public boolean isCommandStationConsistPossible(){
                 return true;
             }
        });

        ConsistToolFrame frame = new ConsistToolFrame();
        Assert.assertNotNull("exists", frame );
        JUnitUtil.dispose(frame);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
