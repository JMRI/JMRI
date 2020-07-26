package jmri.jmrit.operations.locations.schedules;

import java.awt.GraphicsEnvironment;

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
 * @author Paul Bender Copyright (C) 2017
 */
public class SchedulesTableFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SchedulesTableFrame t = new SchedulesTableFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testFrameId() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SchedulesTableFrame stf = new SchedulesTableFrame();
        Assert.assertNotNull("exists", stf);

        ScheduleManager sm = InstanceManager.getDefault(ScheduleManager.class);

        LocationManager lm = InstanceManager.getDefault(LocationManager.class);
        Location l = lm.newLocation("new test location");
        Track t = l.addTrack("track 1", Track.SPUR);

        Schedule s1 = sm.newSchedule("b schedule");
        sm.newSchedule("a schedule");

        ScheduleItem i1 = s1.addItem("BoxCar");
        i1.setRoadName("new road");
        i1.setReceiveLoadName("new load");
        i1.setShipLoadName("new ship load");

        t.setSchedule(s1);

        SchedulesTableModel stm = stf.schedulesModel;
        Assert.assertEquals("Number of rows", 2, stm.getRowCount());
        Assert.assertEquals("Id", "2", stm.getValueAt(0, SchedulesTableModel.ID_COLUMN));

        JemmyUtil.enterClickAndLeave(stf.sortById);
        Assert.assertEquals("Id", "1", stm.getValueAt(0, SchedulesTableModel.ID_COLUMN));

        JUnitUtil.dispose(stf);
    }

    @Test
    public void testFrameDelete() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SchedulesTableFrame stf = new SchedulesTableFrame();
        Assert.assertNotNull("exists", stf);

        ScheduleManager sm = InstanceManager.getDefault(ScheduleManager.class);

        sm.newSchedule("b schedule");
        sm.newSchedule("a schedule");
        sm.newSchedule("c schedule");

        SchedulesTableModel stm = stf.schedulesModel;
        Assert.assertEquals("Number of rows", 3, stm.getRowCount());
        Assert.assertEquals("Name", "a schedule", stm.getValueAt(0, SchedulesTableModel.NAME_COLUMN));
        Assert.assertEquals("Name", "b schedule", stm.getValueAt(1, SchedulesTableModel.NAME_COLUMN));
        Assert.assertEquals("Name", "c schedule", stm.getValueAt(2, SchedulesTableModel.NAME_COLUMN));

        // delete b schedule
        Thread delete = new Thread(new Runnable() {
            @Override
            public void run() {
                stm.setValueAt(null, 1, SchedulesTableModel.DELETE_COLUMN);
            }
        });
        delete.setName("Delete schedule"); // NOI18N
        delete.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return delete.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        // should bring up confirmation dialog
        JemmyUtil.pressDialogButton(Bundle.getMessage("DeleteSchedule?"), Bundle.getMessage("ButtonYes"));

        try {
            delete.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Assert.assertEquals("Number of rows", 2, stm.getRowCount());
        Assert.assertEquals("Name", "a schedule", stm.getValueAt(0, SchedulesTableModel.NAME_COLUMN));
        Assert.assertEquals("Name", "c schedule", stm.getValueAt(1, SchedulesTableModel.NAME_COLUMN));

        JUnitUtil.dispose(stf);
    }

//    @Test
//    public void testFrameEdit() {
//        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//        SchedulesTableFrame stf = new SchedulesTableFrame();
//        Assert.assertNotNull("exists", stf);
//
//        ScheduleManager sm = InstanceManager.getDefault(ScheduleManager.class);
//
//        Schedule s1 = sm.newSchedule("b schedule");
//        sm.newSchedule("a schedule");
//        sm.newSchedule("c schedule");
//
//        SchedulesTableModel stm = stf.schedulesModel;
//        Assert.assertEquals("Number of rows", 3, stm.getRowCount());
//        Assert.assertEquals("Name", "a schedule", stm.getValueAt(0, SchedulesTableModel.NAME_COLUMN));
//        Assert.assertEquals("Name", "b schedule", stm.getValueAt(1, SchedulesTableModel.NAME_COLUMN));
//        Assert.assertEquals("Name", "c schedule", stm.getValueAt(2, SchedulesTableModel.NAME_COLUMN));
//
//        LocationManager lm = InstanceManager.getDefault(LocationManager.class);
//        Location l = lm.newLocation("new test location");
//        Track t = l.addTrack("track 1", Track.SPUR);
//        t.setSchedule(s1);
//
//        // edit b schedule
//        stm.setValueAt(null, 1, SchedulesTableModel.EDIT_COLUMN);
//
//        // this doesn't work, race condition frame is delayed, SwingUtilities.invokeLater(() ->
//        JmriJFrame es = JmriJFrame.getFrame(MessageFormat.format(Bundle.getMessage("TitleScheduleEdit"),
//                new Object[]{"track 1"}));
//        Assert.assertNotNull("Confirm frame exists", es);
//
//        JUnitUtil.dispose(stf);
//    }

    // private final static Logger log =
    // LoggerFactory.getLogger(SchedulesTableFrameTest.class);

}
