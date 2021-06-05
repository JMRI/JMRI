package jmri.jmrit.operations.routes.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.routes.Route;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * @author Daniel Boudreau Copyright (C) 2021
 */
public class RouteBlockingOrderEditFrameActionTest extends OperationsTestCase {
    @Test
    public void testCTor() {
        RouteBlockingOrderEditFrameAction t = new RouteBlockingOrderEditFrameAction();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Route r = JUnitOperationsUtil.createThreeLocationTurnRoute();
        RouteBlockingOrderEditFrameAction a = new RouteBlockingOrderEditFrameAction(r);
        Assert.assertNotNull("exists", a);

        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));

        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("MenuBlockingOrder"));
        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }
}
