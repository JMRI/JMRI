package jmri.jmrit.operations.locations.schedules;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.util.swing.JemmyUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExportSchedulesTest extends OperationsSwingTestCase{

    @Test
    public void testCTor() {
        ExportSchedules t = new ExportSchedules();
        Assert.assertNotNull("exists", t);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }
    
    @Test
    public void testCreateFile() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ExportSchedules exportLoc = new ExportSchedules();
        Assert.assertNotNull("exists", exportLoc);
        
        loadLocations(); //only Test Loc E has a track
        
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Location l1 = lManager.getLocationByName("Test Loc E");
        Track track = l1.getTrackByName("Test Track", Track.SPUR);
        
        // create schedule with one item
        ScheduleManager sManager = InstanceManager.getDefault(ScheduleManager.class);
        Schedule schedule = sManager.newSchedule("test schedule");
        schedule.addItem("Boxcar");
        
        track.setSchedule(schedule);
        
        // should cause export complete dialog to appear
        Thread export = new Thread(new Runnable() {
            @Override
            public void run() {
                exportLoc.writeOperationsScheduleFile();
            }
        });
        export.setName("Export Schedules"); // NOI18N
        export.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return export.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");
        
        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), "OK");
        
        java.io.File file = new java.io.File(ExportSchedules.defaultOperationsFilename());   
        Assert.assertTrue("Confirm file creation", file.exists());        
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ExportCarsTest.class);
}
