package jmri.jmrit.operations.locations.tools;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * @author J. Scott Walton Copyright (C) 2022
 */
public class ImportLocationsActionTest extends OperationsTestCase {

    private final static Logger log = LoggerFactory.getLogger(ImportLocationsActionTest.class);

    @Test
    public void testCTor() {
        ImportLocations t = new ImportLocations();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testImportToEmpty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);
        ExportLocations exportLoc = new ExportLocations();
        JUnitOperationsUtil.loadFiveLocations();
        Thread exportThread = new Thread(new Runnable() {
            @Override
            public void run() {
                exportLoc.writeOperationsLocationFile();
            }
        });
        exportThread.setName("Export Locations"); // NOI18N
        exportThread.start();
        jmri.util.JUnitUtil.waitFor(() -> {
            return exportThread.getState().equals(Thread.State.WAITING);
        }, "Wait for prompt");
        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), Bundle.getMessage("ButtonOK"));
        java.io.File file = new File(ExportLocations.defaultOperationsFilename());
        Assert.assertTrue("Confirm file creation", file.exists());
        int numberOfTracks = 0;
        int numberOfLocations = 0;
        for (Location thisLocation : locationManager.getList()) {
            numberOfLocations++;
            List<Track> trackList = thisLocation.getTracksList();
            for (Track thisTrack : trackList) {
                log.debug("this track is Location {} track {} ", thisLocation.getName(), thisTrack.getName());
                thisLocation.deleteTrack(thisTrack);
                numberOfTracks++;
            }
            locationManager.deregister(thisLocation);
        }
        OperationsXml.save();
        // should have deleted all tracks now;
        List<Location> emptyList = locationManager.getList();
        Assert.assertEquals("Verify that delete was correct", 0, emptyList.size());
        Thread importThread = new ImportLocations() {
            @Override
            protected File getFile() {
                return new File(OperationsXml.getFileLocation() +
                        OperationsXml.getOperationsDirectoryName() +
                        File.separator +
                        ExportLocations.getOperationsFileName());
            }
        };
        importThread.setName("Test Import Locations"); //NOI18N
        importThread.start();
        jmri.util.JUnitUtil.waitFor(() -> {
            return importThread.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");
        JemmyUtil.pressDialogButton(Bundle.getMessage("SuccessfulImport"), Bundle.getMessage("ButtonOK"));
        try {
            importThread.join();
        } catch (InterruptedException e) {
            log.debug("import was interrupted");
        }
         List<Location> newLocations = locationManager.getList();
        Assert.assertEquals( "Expect to have 3 new locations", 3, newLocations.size());

    }

    @Test
    public void testImportToPartial() {

    }


}
