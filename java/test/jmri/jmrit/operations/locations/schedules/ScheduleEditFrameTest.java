package jmri.jmrit.operations.locations.schedules;

import java.awt.GraphicsEnvironment;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ScheduleEditFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location l = new Location("Location Test Attridutes id", "Location Test Name");
        Track trk = new Track("Test id", "Test Name", "Test Type", l);
        ScheduleEditFrame t = new ScheduleEditFrame(new Schedule("Test id", "Test Name"), trk);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testScheduleEditFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Location l2 = lManager.newLocation("Test Loc C");
        l2.setLength(1003);

        Location l = lManager.getLocationByName("Test Loc C");
        Assert.assertNotNull("Location exists", l);
        Track t = l.addTrack("3rd spur track", Track.SPUR);
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

    // private final static Logger log = LoggerFactory.getLogger(ScheduleEditFrameTest.class);
}
