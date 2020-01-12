package jmri.jmrit.operations.locations.schedules;

import java.awt.GraphicsEnvironment;
import java.util.List;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
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
public class ScheduleEditFrameGuiTest extends OperationsTestCase {

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
        JemmyUtil.enterClickAndLeave(f.addScheduleButton);

        // was the schedule created?
        ScheduleManager m = InstanceManager.getDefault(ScheduleManager.class);
        Schedule s = m.getScheduleByName("Test Schedule A");
        Assert.assertNotNull("Test Schedule A exists", s);

        // now add some car types to the schedule
        String carTypes[]=Bundle.getMessage("carTypeNames").split(",");
        f.typeBox.setSelectedItem(carTypes[1]);
        JemmyUtil.enterClickAndLeave(f.addTypeButton);
        f.typeBox.setSelectedItem(carTypes[2]);
        JemmyUtil.enterClickAndLeave(f.addTypeButton);
        f.typeBox.setSelectedItem(carTypes[3]);
        JemmyUtil.enterClickAndLeave(f.addTypeButton);
        // put Tank Food at start of list
        f.typeBox.setSelectedItem(carTypes[4]);
        JemmyUtil.enterClickAndLeave(f.addLocAtTop);
        JemmyUtil.enterClickAndLeave(f.addTypeButton);
        JemmyUtil.enterClickAndLeave(f.saveScheduleButton);

        List<ScheduleItem> list = s.getItemsBySequenceList();
        Assert.assertEquals("number of items", 4, list.size());

        // since this test is internationalized, and the non-english
        // lists are internationalized, we can just check if each of 
        // the types is in the list.
        for( ScheduleItem si: list) {
           boolean flag = false;
           for(int i=1;i<5;i++) {
              if(si.getTypeName().equals(carTypes[i])) {
                 flag = true;
              }
           }
           Assert.assertTrue("type " + si.getTypeName() + " in list",flag);
        }

        JemmyUtil.enterClickAndLeave(f.deleteScheduleButton);
        // Yes to pop up
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("DeleteSchedule?"), Bundle.getMessage("ButtonYes"));
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
        ScheduleItem i2 = s1.addItem(Bundle.getMessage("Caboose"));
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
        t.setSchedule(sch1);

        JComboBox<LocationTrackPair> box3 = sm.getSpursByScheduleComboBox(s1);
        LocationTrackPair ltp = box3.getItemAt(0);

        Assert.assertEquals("Location track pair location", l, ltp.getLocation());
        Assert.assertEquals("Location track pair track", t, ltp.getTrack());

        // remove all schedules
        sm.dispose();

        names = sm.getSchedulesByNameList();
        Assert.assertEquals("There should be no schedules", 0, names.size());
    }
}
