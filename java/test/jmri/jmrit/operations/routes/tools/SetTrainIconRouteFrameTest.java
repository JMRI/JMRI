package jmri.jmrit.operations.routes.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JSpinnerOperator;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SetTrainIconRouteFrameTest extends OperationsTestCase {

    @Test
    public void testCTorNull() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        SetTrainIconRouteFrame t = new SetTrainIconRouteFrame(null);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);

    }

    @Test
    public void testCTorRoute() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        Route route = InstanceManager.getDefault(RouteManager.class).getRouteByName("Southbound Main Route");
        Assert.assertNotNull(route);
        SetTrainIconRouteFrame t = new SetTrainIconRouteFrame(route);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);

    }
    
    @Test
    public void testFrameButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        Route route = InstanceManager.getDefault(RouteManager.class).getRouteByName("Southbound Main Route");
        Assert.assertNotNull(route);
        SetTrainIconRouteFrame f = new SetTrainIconRouteFrame(route);
        Assert.assertNotNull("exists",f);
        
        JemmyUtil.enterClickAndLeaveThreadSafe(f.placeButton);
        // error dialog should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("PanelNotFound"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
        // confirm default
        RouteLocation rl = route.getDepartsRouteLocation();
        Assert.assertEquals("icon position", 25, rl.getTrainIconX());
        
        // modify spinner and update
        JSpinnerOperator so = new JSpinnerOperator(f.spinTrainIconX);
        so.setValue(345);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.applyButton);    
        // confirmation dialog should appear      
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("DoYouWantThisRoute"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        Assert.assertEquals("icon position", 345, rl.getTrainIconX());
        
        // Save changes
        JemmyUtil.enterClickAndLeave(f.saveButton);
        JemmyUtil.enterClickAndLeave(f.nextButton);
        
        // confirm next route location has been loaded
        Assert.assertEquals("spinner value for ", 75, f.spinTrainIconX.getValue());
        
        JemmyUtil.enterClickAndLeave(f.previousButton);
        
        // confirm previous route location has been loaded
        Assert.assertEquals("spinner value for ", 345, f.spinTrainIconX.getValue());
        
        JUnitUtil.dispose(f);

    }

    // private final static Logger log = LoggerFactory.getLogger(SetTrainIconRouteFrameTest.class);

}
