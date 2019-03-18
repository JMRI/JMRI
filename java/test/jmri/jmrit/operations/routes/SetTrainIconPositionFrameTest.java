package jmri.jmrit.operations.routes;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.netbeans.jemmy.operators.JSpinnerOperator;

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

        SetTrainIconPositionFrame t = new SetTrainIconPositionFrame();
        Assert.assertNotNull("exists", t);

        // no location selected
        JemmyUtil.enterClickAndLeave(t.placeButton);

        // error dialog should appear
        JemmyUtil.pressDialogButton(t, Bundle.getMessage("NoLocationSelected"), Bundle.getMessage("ButtonOK"));

        // select location, no panel error
        t.locationBox.setSelectedIndex(1);

        JemmyUtil.enterClickAndLeave(t.placeButton);

        // error dialog should appear
        JemmyUtil.pressDialogButton(t, Bundle.getMessage("PanelNotFound"), Bundle.getMessage("ButtonOK"));

        // confirm train icon defaults for location and route
        Location loc = InstanceManager.getDefault(LocationManager.class).getLocationByName("North End Staging");
        Assert.assertNotNull(loc);
        Assert.assertEquals("icon position", 0, loc.getTrainIconSouth().x);

        Route route = InstanceManager.getDefault(RouteManager.class).getRouteByName("Southbound Main Route");
        Assert.assertNotNull(route);
        RouteLocation rl = route.getDepartsRouteLocation();
        Assert.assertEquals("icon position", 25, rl.getTrainIconX());

        // modify spinner and update
        JSpinnerOperator so = new JSpinnerOperator(t.spinTrainIconSouthX);
        so.setValue(234);
        JemmyUtil.enterClickAndLeave(t.applyButton);

        // confirm dialog should appear
        JemmyUtil.pressDialogButton(t,
                MessageFormat.format(Bundle.getMessage("UpdateTrainIcon"), new Object[]{loc.getName()}),
                Bundle.getMessage("ButtonYes"));

        // confirm that location and routes have been modified
        Assert.assertEquals("icon position", 234, loc.getTrainIconSouth().x);
        Assert.assertEquals("icon position", 234, rl.getTrainIconX());

        so.setValue(567);
        JemmyUtil.enterClickAndLeave(t.saveButton);

        // confirm dialog should appear
        JemmyUtil.pressDialogButton(t,
                MessageFormat.format(Bundle.getMessage("UpdateTrainIcon"), new Object[]{loc.getName()}),
                Bundle.getMessage("ButtonYes"));

        // confirm that location has been modified, not routes
        Assert.assertEquals("icon position", 567, loc.getTrainIconSouth().x);
        Assert.assertEquals("icon position", 234, rl.getTrainIconX());

        JUnitUtil.dispose(t);
    }

    // private final static Logger log = LoggerFactory.getLogger(SetTrainIconPositionFrameTest.class);
}
