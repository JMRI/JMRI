package jmri.jmrit.operations.routes.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Daniel Boudreau Copyright (C) 2021
 */
public class RouteBlockingOrderEditFrameTest extends OperationsTestCase {
    @Test
    public void testFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Route r = JUnitOperationsUtil.createThreeLocationTurnRoute();
        RouteBlockingOrderEditFrame t = new RouteBlockingOrderEditFrame(r);
        Assert.assertNotNull("exists", t);
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("MenuBlockingOrder"));
        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testResetButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Route r = JUnitOperationsUtil.createThreeLocationTurnRoute();
        for (RouteLocation rl : r.getLocationsBySequenceList()) {
            Assert.assertEquals("blocking order default", 0, rl.getBlockingOrder());
        }
        RouteBlockingOrderEditFrame rboef = new RouteBlockingOrderEditFrame(r);
        Assert.assertNotNull("exists", rboef);
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("MenuBlockingOrder"));
        Assert.assertNotNull("frame exists", f);

        int i = 1;
        for (RouteLocation rl : r.getLocationsBySequenceList()) {
            Assert.assertEquals("blocking before reset", i++, rl.getBlockingOrder());
        }
        
        for (RouteLocation rl : r.getLocationsBySequenceList()) {
            rl.setBlockingOrder(0);
        }
        
        JemmyUtil.enterClickAndLeave(rboef.resetRouteButton);
        i = 1;
        for (RouteLocation rl : r.getLocationsBySequenceList()) {
            Assert.assertEquals("blocking order after reset", i++, rl.getBlockingOrder());
        }
        JUnitUtil.dispose(f);
    }
}
