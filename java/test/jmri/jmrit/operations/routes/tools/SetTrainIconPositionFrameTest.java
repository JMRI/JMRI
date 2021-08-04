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

        // confirm train icon defaults for location and route
        Location loc = InstanceManager.getDefault(LocationManager.class).getLocationByName("North End Staging");
        Assert.assertNotNull(loc);
        Assert.assertEquals("icon position", 0, loc.getTrainIconSouth().x);

        Route route = InstanceManager.getDefault(RouteManager.class).getRouteByName("Southbound Main Route");
        Assert.assertNotNull(route);
        RouteLocation rl = route.getDepartsRouteLocation();
        Assert.assertEquals("icon position", 25, rl.getTrainIconX());

        // modify spinner and update
        JSpinnerOperator so = new JSpinnerOperator(f.spinTrainIconSouthX);
        so.setValue(234);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.applyButton);
        // confirm dialog should appear
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("UpdateTrainIcon"), new Object[]{loc.getName()}),
                Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        
        // confirm that location and routes have been modified
        Assert.assertEquals("icon position", 234, loc.getTrainIconSouth().x);
        Assert.assertEquals("icon position", 234, rl.getTrainIconX());

        so.setValue(567);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        // confirm dialog should appear
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("UpdateTrainIcon"), new Object[]{loc.getName()}),
                Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        
        // confirm that location has been modified, not routes
        Assert.assertEquals("icon position", 567, loc.getTrainIconSouth().x);
        Assert.assertEquals("icon position", 234, rl.getTrainIconX());

        JUnitUtil.dispose(f);
    }

    // private final static Logger log = LoggerFactory.getLogger(SetTrainIconPositionFrameTest.class);
}
