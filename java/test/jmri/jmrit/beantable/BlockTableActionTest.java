package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.Block;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for the jmri.jmrit.beantable.BlockTableAction class
 *
 * @author	Bob Jacobsen Copyright 2004, 2007, 2008
 */
public class BlockTableActionTest {

    @Test
    public void testCreate() {
        BlockTableAction ba = new BlockTableAction();
        Assert.assertNotNull(ba);
        Assert.assertNull(ba.f); // frame should be null until action invoked
    }

    @Test
    public void testInvoke() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new BlockTableAction().actionPerformed(null);

        JFrame f = JFrameOperator.waitJFrame(Bundle.getMessage("TitleBlockTable"), true, true);
        Assert.assertNotNull(f);
        // create a couple blocks, and see if they show
        InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock("IB1", "block 1");

        Block b2 = InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock("IB2", "block 2");
        Assert.assertNotNull(b2);
        b2.setDirection(jmri.Path.EAST);
        // TODO: assert blocks show in frame
        f.dispose();
    }

    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
