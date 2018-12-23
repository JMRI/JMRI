package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * Tests for the Operations Locations GUI class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 */
public class LocationEditFrameTest extends OperationsTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;

    @Test
    public void testLocationEditFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.loadFiveLocations();

        LocationEditFrame f = new LocationEditFrame(null);
        f.setTitle("Test Add Location Frame");

        f.locationNameTextField.setText("New Test Location");
        JemmyUtil.enterClickAndLeave(f.addLocationButton);

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 6 locations", 6, lManager.getLocationsByNameList().size());
        Location newLoc = lManager.getLocationByName("New Test Location");

        Assert.assertNotNull(newLoc);

        // add a yard track
        JemmyUtil.enterClickAndLeave(f.addYardButton);

        // add an interchange track
        JemmyUtil.enterClickAndLeave(f.addInterchangeButton);

        // add a staging track
        JemmyUtil.enterClickAndLeave(f.addStagingButton);

        // add a yard track
        JemmyUtil.enterClickAndLeave(f.addYardButton);

        f.locationNameTextField.setText("Newer Test Location");
        JemmyUtil.enterClickAndLeave(f.saveLocationButton);

        Assert.assertEquals("changed location name", "Newer Test Location", newLoc.getName());

        // test delete button
        JemmyUtil.enterClickAndLeave(f.deleteLocationButton);
        Assert.assertEquals("should be 6 locations", 6, lManager.getLocationsByNameList().size());
        // confirm delete dialog window should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("deletelocation?"), Bundle.getMessage("ButtonYes"));
        // location now deleted
        Assert.assertEquals("should be 5 locations", 5, lManager.getLocationsByNameList().size());

        JUnitUtil.dispose(f);
    }
}
