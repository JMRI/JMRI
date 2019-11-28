package jmri.jmrit.operations.routes;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JSpinnerOperator;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
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
        SetTrainIconRouteFrame t = new SetTrainIconRouteFrame(null);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testCTorRoute() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Route route = InstanceManager.getDefault(RouteManager.class).getRouteByName("Southbound Main Route");
        Assert.assertNotNull(route);
        SetTrainIconRouteFrame t = new SetTrainIconRouteFrame(route);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testFrameButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Route route = InstanceManager.getDefault(RouteManager.class).getRouteByName("Southbound Main Route");
        Assert.assertNotNull(route);
        SetTrainIconRouteFrame t = new SetTrainIconRouteFrame(route);
        Assert.assertNotNull("exists",t);
        
        JemmyUtil.enterClickAndLeave(t.placeButton);

        // error dialog should appear
        JemmyUtil.pressDialogButton(t, Bundle.getMessage("PanelNotFound"), Bundle.getMessage("ButtonOK"));

        // confirm default
        RouteLocation rl = route.getDepartsRouteLocation();
        Assert.assertEquals("icon position", 25, rl.getTrainIconX());
        
        // modify spinner and update
        JSpinnerOperator so = new JSpinnerOperator(t.spinTrainIconX);
        so.setValue(345);
        JemmyUtil.enterClickAndLeave(t.applyButton);
        
        // confirmation dialog should appear      
        JemmyUtil.pressDialogButton(t, Bundle.getMessage("DoYouWantThisRoute"), Bundle.getMessage("ButtonYes"));

        Assert.assertEquals("icon position", 345, rl.getTrainIconX());
        
        // Save changes
        JemmyUtil.enterClickAndLeave(t.saveButton);
        
        JemmyUtil.enterClickAndLeave(t.nextButton);
        
        // confirm next route location has been loaded
        Assert.assertEquals("spinner value for ", 75, t.spinTrainIconX.getValue());
        
        JemmyUtil.enterClickAndLeave(t.previousButton);
        
        // confirm previous route location has been loaded
        Assert.assertEquals("spinner value for ", 345, t.spinTrainIconX.getValue());
        
        JUnitUtil.dispose(t);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();
 
        JUnitOperationsUtil.initOperationsData();
    }

    // private final static Logger log = LoggerFactory.getLogger(SetTrainIconRouteFrameTest.class);

}
