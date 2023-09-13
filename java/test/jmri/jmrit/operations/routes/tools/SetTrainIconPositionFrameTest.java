package jmri.jmrit.operations.routes.tools;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JSpinnerOperator;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.routes.*;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SetTrainIconPositionFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SetTrainIconPositionFrame t = new SetTrainIconPositionFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testFrameButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();

        SetTrainIconPositionFrame f = new SetTrainIconPositionFrame();
        Assert.assertNotNull("exists", f);

        // no location selected
        JemmyUtil.enterClickAndLeaveThreadSafe(f.placeButton);
        // error dialog should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("NoLocationSelected"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
        // select location, no panel error
        f.locationBox.setSelectedIndex(1);

        JemmyUtil.enterClickAndLeaveThreadSafe(f.placeButton);

        // error dialog should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("PanelNotFound"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
        // confirm train icon defaults for location and route
        Location loc = InstanceManager.getDefault(LocationManager.class).getLocationByName("North End Staging");
        Assert.assertNotNull(loc);
        Assert.assertEquals("icon position", 0, loc.getTrainIconSouth().x);

        Route route = InstanceManager.getDefault(RouteManager.class).getRouteByName("Southbound Main Route");
        Assert.assertNotNull(route);
        RouteLocation rl = route.getDepartsRouteLocation();
        Assert.assertEquals("icon position", 25, rl.getTrainIconX());

        // modify spinner and update
        JSpinnerOperator soSouthX = new JSpinnerOperator(f.spinTrainIconSouthX);
        soSouthX.setValue(234);
        JSpinnerOperator soSouthY = new JSpinnerOperator(f.spinTrainIconSouthY);
        soSouthY.setValue(432);
        
        JSpinnerOperator soNorthX = new JSpinnerOperator(f.spinTrainIconNorthX);
        soNorthX.setValue(515);
        JSpinnerOperator soNorthY = new JSpinnerOperator(f.spinTrainIconNorthY);
        soNorthY.setValue(525);
        
        JSpinnerOperator soEastX = new JSpinnerOperator(f.spinTrainIconEastX);
        soEastX.setValue(213);
        JSpinnerOperator soEastY = new JSpinnerOperator(f.spinTrainIconEastY);
        soEastY.setValue(312);
        
        JSpinnerOperator soWestX = new JSpinnerOperator(f.spinTrainIconWestX);
        soWestX.setValue(45);
        JSpinnerOperator soWestY = new JSpinnerOperator(f.spinTrainIconWestY);
        soWestY.setValue(54);
        
        JSpinnerOperator soRangeX = new JSpinnerOperator(f.spinTrainIconRangeX);
        soRangeX.setValue(36);
        JSpinnerOperator soRangeY = new JSpinnerOperator(f.spinTrainIconRangeY);
        soRangeY.setValue(42);
        
        JemmyUtil.enterClickAndLeaveThreadSafe(f.applyButton);
        // confirm dialog should appear
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("UpdateTrainIcon"), new Object[]{loc.getName()}),
                Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        
        // confirm that location and routes have been modified
        Assert.assertEquals("icon position", 234, loc.getTrainIconSouth().x);
        Assert.assertEquals("icon position", 432, loc.getTrainIconSouth().y);
        
        Assert.assertEquals("icon position", 234, rl.getTrainIconX());
        Assert.assertEquals("icon position", 432, rl.getTrainIconY());
        
        Assert.assertEquals("icon position", 515, loc.getTrainIconNorth().x);
        Assert.assertEquals("icon position", 525, loc.getTrainIconNorth().y);
        
        Assert.assertEquals("icon position", 213, loc.getTrainIconEast().x);
        Assert.assertEquals("icon position", 312, loc.getTrainIconEast().y);
        
        Assert.assertEquals("icon position", 45, loc.getTrainIconWest().x);
        Assert.assertEquals("icon position", 54, loc.getTrainIconWest().y);
        
        Assert.assertEquals("icon range", 36, loc.getTrainIconRangeX());
        Assert.assertEquals("icon range", 42, loc.getTrainIconRangeY());

        soSouthX.setValue(567);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        // confirm dialog should appear
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("UpdateTrainIcon"), new Object[]{loc.getName()}),
                Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        
        // confirm that location has been modified, not routes
        Assert.assertEquals("icon position", 567, loc.getTrainIconSouth().x);
        Assert.assertEquals("icon position", 234, rl.getTrainIconX());
        
        // confirm icon positions get reset
        f.locationBox.setSelectedIndex(0);
        Assert.assertEquals("spinner value", 0, f.spinTrainIconSouthX.getValue());  

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        SetTrainIconPositionFrame f = new SetTrainIconPositionFrame();
        // select location
        f.locationBox.setSelectedIndex(1);
        // now close window with save button
        Setup.setCloseWindowOnSaveEnabled(true);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        Location loc = InstanceManager.getDefault(LocationManager.class).getLocationByName("North End Staging");
        // confirm dialog should appear
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("UpdateTrainIcon"), new Object[]{loc.getName()}),
                Bundle.getMessage("ButtonYes"));
        JmriJFrame frame = JmriJFrame.getFrame(f.getTitle());
        Assert.assertNull("does not exist", frame);
    }

    // private final static Logger log = LoggerFactory.getLogger(SetTrainIconPositionFrameTest.class);
}
