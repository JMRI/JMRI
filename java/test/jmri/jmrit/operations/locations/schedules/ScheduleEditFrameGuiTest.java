//ScheduleEditFrameTest.java
package jmri.jmrit.operations.locations.schedules;

import java.awt.GraphicsEnvironment;
import java.util.List;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
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
public class ScheduleEditFrameGuiTest extends OperationsSwingTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;

    @Test
    public void testScheduleEditFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Location l2 = lManager.newLocation("Test Loc C");
        l2.setLength(1003);

        Location l = lManager.getLocationByName("Test Loc C");
        Assert.assertNotNull("Location exists", l);
        Track t = l.addTrack("3rd siding track", Track.SPUR);
        Assert.assertNotNull("Track exists", t);
        ScheduleEditFrame f = new ScheduleEditFrame(null, t);
        f.setTitle("Test Schedule Frame");
        f.scheduleNameTextField.setText("Test Schedule A");
        f.commentTextField.setText("Test Comment");
        enterClickAndLeave(f.addScheduleButton);

        // was the schedule created?
        ScheduleManager m = InstanceManager.getDefault(ScheduleManager.class);
        Schedule s = m.getScheduleByName("Test Schedule A");
        Assert.assertNotNull("Test Schedule A exists", s);

        // now add some car types to the schedule
        f.typeBox.setSelectedItem("Boxcar");
        enterClickAndLeave(f.addTypeButton);
        f.typeBox.setSelectedItem("Flatcar");
        enterClickAndLeave(f.addTypeButton);
        f.typeBox.setSelectedItem("Coilcar");
        enterClickAndLeave(f.addTypeButton);
        // put Tank Food at start of list
        f.typeBox.setSelectedItem("Tank Food");
        enterClickAndLeave(f.addLocAtTop);
        enterClickAndLeave(f.addTypeButton);
        enterClickAndLeave(f.saveScheduleButton);

        List<ScheduleItem> list = s.getItemsBySequenceList();
        Assert.assertEquals("number of items", 4, list.size());

        ScheduleItem si = list.get(0);
        Assert.assertEquals("1st type", "Tank Food", si.getTypeName());
        si = list.get(1);
        Assert.assertEquals("2nd type", "Boxcar", si.getTypeName());
        si = list.get(2);
        Assert.assertEquals("3rd type", "Flatcar", si.getTypeName());
        si = list.get(3);
        Assert.assertEquals("3rd type", "Coilcar", si.getTypeName());

        enterClickAndLeave(f.deleteScheduleButton);
        // Yes to pop up
        pressDialogButton(f, Bundle.getMessage("DeleteSchedule?"), Bundle.getMessage("ButtonYes"));
        s = m.getScheduleByName("Test Schedule A");
        Assert.assertNull("Test Schedule A exists", s);

        JUnitUtil.dispose(f);
    }

    @Test
    public void testScheduleComboBoxes() {
        LocationManager lm = InstanceManager.getDefault(LocationManager.class);
        Location l = lm.newLocation("new test location");
        Track t = l.addTrack("track 1", Track.SPUR);

        ScheduleManager sm = InstanceManager.getDefault(ScheduleManager.class);

        // clear out any previous schedules
        sm.dispose();
        sm = InstanceManager.getDefault(ScheduleManager.class);

        Schedule s1 = sm.newSchedule("new schedule");
        Schedule s2 = sm.newSchedule("newer schedule");
        ScheduleItem i1 = s1.addItem("BoxCar");
        i1.setRoadName("new road");
        i1.setReceiveLoadName("new load");
        i1.setShipLoadName("new ship load");
        ScheduleItem i2 = s1.addItem("Caboose");
        i2.setRoadName("road");
        i2.setReceiveLoadName("load");
        i2.setShipLoadName("ship load");

        Assert.assertEquals("1 First schedule name", "new schedule", s1.getName());
        Assert.assertEquals("1 First schedule name", "newer schedule", s2.getName());

        List<Schedule> names = sm.getSchedulesByNameList();
        Assert.assertEquals("There should be 2 schedules", 2, names.size());
        Schedule sch1 = names.get(0);
        Schedule sch2 = names.get(1);
        Assert.assertEquals("2 First schedule name", "new schedule", sch1.getName());
        Assert.assertEquals("2 First schedule name", "newer schedule", sch2.getName());
        Assert.assertEquals("Schedule 1", sch1, sm.getScheduleByName("new schedule"));
        Assert.assertEquals("Schedule 2", sch2, sm.getScheduleByName("newer schedule"));

        JComboBox<Schedule> box = sm.getComboBox();
        Assert.assertEquals("3 First schedule name", null, box.getItemAt(0));
        Assert.assertEquals("3 First schedule name", sch1, box.getItemAt(1));
        Assert.assertEquals("3 First schedule name", sch2, box.getItemAt(2));

        JComboBox<LocationTrackPair> box2 = sm.getSpursByScheduleComboBox(s1);
        Assert.assertEquals("First siding name", null, box2.getItemAt(0));

        // now add a schedule to siding
        t.setScheduleId(sch1.getId());

        JComboBox<LocationTrackPair> box3 = sm.getSpursByScheduleComboBox(s1);
        LocationTrackPair ltp = box3.getItemAt(0);

        Assert.assertEquals("Location track pair location", l, ltp.getLocation());
        Assert.assertEquals("Location track pair track", t, ltp.getTrack());

        // remove all schedules
        sm.dispose();

        names = sm.getSchedulesByNameList();
        Assert.assertEquals("There should be no schedules", 0, names.size());

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

    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        loadLocations();
    }

    // The minimal setup for log4J
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
