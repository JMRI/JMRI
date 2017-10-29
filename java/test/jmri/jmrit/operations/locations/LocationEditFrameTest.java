//LocationEditFrameTest.java
package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations Locations GUI class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 */
public class LocationEditFrameTest extends OperationsSwingTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;

    @Test
    public void testLocationEditFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        loadLocations();

        LocationEditFrame f = new LocationEditFrame(null);
        f.setTitle("Test Add Location Frame");

        f.locationNameTextField.setText("New Test Location");
        enterClickAndLeave(f.addLocationButton);

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 6 locations", 6, lManager.getLocationsByNameList().size());
        Location newLoc = lManager.getLocationByName("New Test Location");

        Assert.assertNotNull(newLoc);

        // add a yard track
        enterClickAndLeave(f.addYardButton);

        // add an interchange track
        enterClickAndLeave(f.addInterchangeButton);

        // add a staging track
        enterClickAndLeave(f.addStagingButton);

        // add a yard track
        enterClickAndLeave(f.addYardButton);

        f.locationNameTextField.setText("Newer Test Location");
        enterClickAndLeave(f.saveLocationButton);

        Assert.assertEquals("changed location name", "Newer Test Location", newLoc.getName());

        // test delete button
        enterClickAndLeave(f.deleteLocationButton);
        Assert.assertEquals("should be 6 locations", 6, lManager.getLocationsByNameList().size());
        // confirm delete dialog window should appear
        pressDialogButton(f, Bundle.getMessage("deletelocation?"), Bundle.getMessage("ButtonYes"));
        // location now deleted
        Assert.assertEquals("should be 5 locations", 5, lManager.getLocationsByNameList().size());

        JUnitUtil.dispose(f);
    }

    private void loadLocations() {
        // create 5 locations
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Location l1 = lManager.newLocation("Test Loc E");
        l1.setLength(1001);
        Location l2 = lManager.newLocation("Test Loc D");
        l2.setLength(1002);
        Location l3 = lManager.newLocation("Test Loc C");
        l3.setLength(1003);
        Location l4 = lManager.newLocation("Test Loc B");
        l4.setLength(1004);
        Location l5 = lManager.newLocation("Test Loc A");
        l5.setLength(1005);

    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
