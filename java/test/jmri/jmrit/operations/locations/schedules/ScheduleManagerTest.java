package jmri.jmrit.operations.locations.schedules;

import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Operations Locations class Last manually cross-checked on
 * 20090131
 *
 * Still to do: ScheduleItem: XML read/write Schedule: Register, List, XML
 * read/write Track: AcceptsDropTrain, AcceptsDropRoute Track:
 * AcceptsPickupTrain, AcceptsPickupRoute Track: CheckScheduleValid Track: XML
 * read/write Location: Track support <-- I am here Location: XML read/write
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
 */
public class ScheduleManagerTest extends OperationsTestCase {

    @Test
    public void testScheduleManager() {
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

        // Remove references to swing
        // JComboBox box = sm.getComboBox();
        // Assert.assertEquals("3 First schedule name", "", box.getItemAt(0));
        // Assert.assertEquals("3 First schedule name", sch1, box.getItemAt(1));
        // Assert.assertEquals("3 First schedule name", sch2, box.getItemAt(2));
        //
        // JComboBox box2 = sm.getSidingsByScheduleComboBox(s1);
        // Assert.assertEquals("First siding name", null, box2.getItemAt(0));
        // now add a schedule to siding
        t.setSchedule(sch1);

		// JComboBox box3 = sm.getSidingsByScheduleComboBox(s1);
        // LocationTrackPair ltp = (LocationTrackPair)box3.getItemAt(0);
        // Assert.assertEquals("Location track pair location", l, ltp.getLocation());
        // Assert.assertEquals("Location track pair track", t, ltp.getTrack());
        Assert.assertEquals("1 Schedule Item 1 type", "BoxCar", i1.getTypeName());
        Assert.assertEquals("1 Schedule Item 1 road", "new road", i1.getRoadName());
        Assert.assertEquals("1 Schedule Item 1 load", "new load", i1.getReceiveLoadName());
        Assert.assertEquals("1 Schedule Item 1 ship", "new ship load", i1.getShipLoadName());

        Assert.assertEquals("1 Schedule Item 2 type", Bundle.getMessage("Caboose"), i2.getTypeName());
        Assert.assertEquals("1 Schedule Item 2 road", "road", i2.getRoadName());
        Assert.assertEquals("1 Schedule Item 2 load", "load", i2.getReceiveLoadName());
        Assert.assertEquals("1 Schedule Item 2 ship", "ship load", i2.getShipLoadName());

        sm.replaceRoad("new road", "replaced road");

        Assert.assertEquals("2 Schedule Item 1 type", "BoxCar", i1.getTypeName());
        Assert.assertEquals("2 Schedule Item 1 road", "replaced road", i1.getRoadName());
        Assert.assertEquals("2 Schedule Item 1 load", "new load", i1.getReceiveLoadName());
        Assert.assertEquals("2 Schedule Item 1 ship", "new ship load", i1.getShipLoadName());

        Assert.assertEquals("2 Schedule Item 2 type", Bundle.getMessage("Caboose"), i2.getTypeName());
        Assert.assertEquals("2 Schedule Item 2 road", "road", i2.getRoadName());
        Assert.assertEquals("2 Schedule Item 2 load", "load", i2.getReceiveLoadName());
        Assert.assertEquals("2 Schedule Item 2 ship", "ship load", i2.getShipLoadName());

        sm.replaceType("BoxCar", "replaced car type");

        Assert.assertEquals("3 Schedule Item 1 type", "replaced car type", i1.getTypeName());
        Assert.assertEquals("3 Schedule Item 1 road", "replaced road", i1.getRoadName());
        Assert.assertEquals("3 Schedule Item 1 load", "new load", i1.getReceiveLoadName());
        Assert.assertEquals("3 Schedule Item 1 ship", "new ship load", i1.getShipLoadName());

        Assert.assertEquals("3 Schedule Item 2 type", Bundle.getMessage("Caboose"), i2.getTypeName());
        Assert.assertEquals("3 Schedule Item 2 road", "road", i2.getRoadName());
        Assert.assertEquals("3 Schedule Item 2 load", "load", i2.getReceiveLoadName());
        Assert.assertEquals("3 Schedule Item 2 ship", "ship load", i2.getShipLoadName());

        sm.replaceType(Bundle.getMessage("Caboose"), "BoxCar");

        Assert.assertEquals("4 Schedule Item 1 type", "replaced car type", i1.getTypeName());
        Assert.assertEquals("4 Schedule Item 1 road", "replaced road", i1.getRoadName());
        Assert.assertEquals("4 Schedule Item 1 load", "new load", i1.getReceiveLoadName());
        Assert.assertEquals("4 Schedule Item 1 ship", "new ship load", i1.getShipLoadName());

        Assert.assertEquals("4 Schedule Item 2 type", "BoxCar", i2.getTypeName());
        Assert.assertEquals("4 Schedule Item 2 road", "road", i2.getRoadName());
        Assert.assertEquals("4 Schedule Item 2 load", "load", i2.getReceiveLoadName());
        Assert.assertEquals("4 Schedule Item 2 ship", "ship load", i2.getShipLoadName());

        sm.replaceLoad("BoxCar", "load", "new load");

        Assert.assertEquals("5 Schedule Item 1 type", "replaced car type", i1.getTypeName());
        Assert.assertEquals("5 Schedule Item 1 road", "replaced road", i1.getRoadName());
        Assert.assertEquals("5 Schedule Item 1 load", "new load", i1.getReceiveLoadName());
        Assert.assertEquals("5 Schedule Item 1 ship", "new ship load", i1.getShipLoadName());

        Assert.assertEquals("5 Schedule Item 2 type", "BoxCar", i2.getTypeName());
        Assert.assertEquals("5 Schedule Item 2 road", "road", i2.getRoadName());
        Assert.assertEquals("5 Schedule Item 2 load", "new load", i2.getReceiveLoadName());
        Assert.assertEquals("5 Schedule Item 2 ship", "ship load", i2.getShipLoadName());

        sm.replaceLoad("BoxCar", "new load", "next load");

        Assert.assertEquals("6 Schedule Item 1 type", "replaced car type", i1.getTypeName());
        Assert.assertEquals("6 Schedule Item 1 road", "replaced road", i1.getRoadName());
        Assert.assertEquals("6 Schedule Item 1 load", "new load", i1.getReceiveLoadName());
        Assert.assertEquals("6 Schedule Item 1 ship", "new ship load", i1.getShipLoadName());

        Assert.assertEquals("6 Schedule Item 2 type", "BoxCar", i2.getTypeName());
        Assert.assertEquals("6 Schedule Item 2 road", "road", i2.getRoadName());
        Assert.assertEquals("6 Schedule Item 2 load", "next load", i2.getReceiveLoadName());
        Assert.assertEquals("6 Schedule Item 2 ship", "ship load", i2.getShipLoadName());

        // remove all schedules
        sm.dispose();

        names = sm.getSchedulesByNameList();
        Assert.assertEquals("There should be no schedules", 0, names.size());
    }
}
