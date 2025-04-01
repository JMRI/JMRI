package jmri.jmrit.operations.locations.schedules.tools;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExportSchedulesTest extends OperationsTestCase{

    @Test
    public void testCTor() {
        ExportSchedules t = new ExportSchedules();
        Assert.assertNotNull("exists", t);
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testCreateFile() {

        ExportSchedules exportLoc = new ExportSchedules();
        Assert.assertNotNull("exists", exportLoc);

        JUnitOperationsUtil.loadFiveLocations(); //only Test Loc E has a track

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Location l1 = lManager.getLocationByName("Test Loc E");
        Track track = l1.getTrackByName("Test Track", Track.SPUR);

        // create schedule with one item
        ScheduleManager sManager = InstanceManager.getDefault(ScheduleManager.class);
        Schedule schedule = sManager.newSchedule("test schedule");
        schedule.addItem("Boxcar");

        track.setSchedule(schedule);

        // should cause export complete dialog to appear
        Thread export = new Thread(exportLoc::writeOperationsScheduleFile);
        export.setName("Export Schedules"); // NOI18N
        export.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return export.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), Bundle.getMessage("ButtonOK"));

        jmri.util.JUnitUtil.waitFor(() -> !export.isAlive(), "wait for export to complete");

        java.io.File file = new java.io.File(ExportSchedules.defaultOperationsFilename());   
        Assert.assertTrue("Confirm file creation", file.exists());
    }

    // private final static Logger log = LoggerFactory.getLogger(ExportCarsTest.class);
}
