package jmri.jmrit.operations.routes.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteEditFrame;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RouteCopyFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RouteCopyFrame t = new RouteCopyFrame(null);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testCopyNoRouteSelected() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.createThreeLocationRoute();
        RouteCopyFrame rcf = new RouteCopyFrame(null);
        Assert.assertNotNull("exists", rcf);
        
        // route not selected
        rcf.routeNameTextField.setText("TestCopyRouteName");
        JemmyUtil.enterClickAndLeave(rcf.copyButton);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        Assert.assertEquals("routes 1", 1, rmanager.getRoutesByNameList().size());       
        JemmyUtil.pressDialogButton(rcf, Bundle.getMessage("CopyRoute"), Bundle.getMessage("ButtonOK"));
        
        JUnitUtil.dispose(rcf);
    }
    
    @Test
    public void testCopy() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Route r = JUnitOperationsUtil.createThreeLocationRoute();
        RouteCopyFrame rcf = new RouteCopyFrame(r);
        Assert.assertNotNull("exists", rcf);
        
        // no new route name
        JemmyUtil.enterClickAndLeaveThreadSafe(rcf.copyButton);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        Assert.assertEquals("routes 1", 1, rmanager.getRoutesByNameList().size());       
        JemmyUtil.pressDialogButton(rcf, Bundle.getMessage("EnterRouteName"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(rcf);
        
        // name too long
        rcf.routeNameTextField.setText("abcdefghijklmnopqrstuvwxwyz");
        JemmyUtil.enterClickAndLeaveThreadSafe(rcf.copyButton);
        Assert.assertEquals("routes 1", 1, rmanager.getRoutesByNameList().size());       
        JemmyUtil.pressDialogButton(rcf, Bundle.getMessage("CanNotAddRoute"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(rcf);
        
        // good route name
        rcf.routeNameTextField.setText("TestCopyRouteName");
        JemmyUtil.enterClickAndLeave(rcf.copyButton);
        Assert.assertEquals("routes 2", 2, rmanager.getRoutesByNameList().size());
        Assert.assertNotNull("Route exists", rmanager.getRouteByName("TestCopyRouteName"));
        
        RouteEditFrame editRouteFrame = (RouteEditFrame) JmriJFrame.getFrame(Bundle.getMessage("TitleRouteEdit"));
        Assert.assertNotNull("Edit frame", editRouteFrame);
        
        JUnitUtil.dispose(editRouteFrame);
        
        // same route name, error
        JemmyUtil.enterClickAndLeaveThreadSafe(rcf.copyButton);
        Assert.assertEquals("routes 2", 2, rmanager.getRoutesByNameList().size());
        JemmyUtil.pressDialogButton(rcf, Bundle.getMessage("CanNotAddRoute"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(rcf);
        
        editRouteFrame = (RouteEditFrame) JmriJFrame.getFrame(Bundle.getMessage("TitleRouteEdit"));
        Assert.assertNull("Edit frame", editRouteFrame);

        JUnitUtil.dispose(rcf);
    }

    // private final static Logger log = LoggerFactory.getLogger(RouteCopyFrameTest.class);

}
