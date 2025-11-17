package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.*;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;

/**
 * @author J. Scott Walton Copyright (C) 2022
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class ImportLocationsTest extends OperationsTestCase {

    private final static Logger log = LoggerFactory.getLogger(ImportLocationsTest.class);

    @Test
    public void testCTor() {
        ImportLocations t = new ImportLocations();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testImport() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);
        ExportLocations exportLoc = new ExportLocations();
        JUnitOperationsUtil.createSevenNormalLocations();
        Thread exportThread = new Thread(new Runnable() {
            @Override
            public void run() {
                exportLoc.writeOperationsLocationFile();
            }
        });

        // set comment for tracks, these are the last elements to be exported / imported
        Location gulf = locationManager.getLocationByName("Gulf");
        Track gulfYard2 = gulf.getTrackByName("Gulf Yard 2", null);
        gulfYard2.setComment("Comment for gulf yard 2");
        gulfYard2.setCommentSetout("Setout comment for gulf yard 2");

        exportThread.setName("Export Locations"); // NOI18N
        exportThread.start();
        jmri.util.JUnitUtil.waitFor(() -> {
            return exportThread.getState().equals(Thread.State.WAITING);
        }, "Wait for prompt");
        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), Bundle.getMessage("ButtonOK"));
        // get exported file
        java.io.File file = new File(ExportLocations.defaultOperationsFilename());
        Assert.assertTrue("Confirm file creation", file.exists());
        // delete all tracks and locations
        for (Location thisLocation : locationManager.getList()) {
            List<Track> trackList = thisLocation.getTracksList();
            for (Track thisTrack : trackList) {
                log.debug("this track is Location {} track {} ", thisLocation.getName(), thisTrack.getName());
                thisLocation.deleteTrack(thisTrack);
            }
            locationManager.deregister(thisLocation);
        }
        OperationsXml.save();
        // should have deleted all tracks now;
        List<Location> emptyList = locationManager.getList();
        Assert.assertEquals("Verify delete", 0, emptyList.size());
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
        Assert.assertEquals( "Expect to have 7 new locations", 7, newLocations.size());

        // confirm comment for tracks
        gulf = locationManager.getLocationByName("Gulf");
        gulfYard2 = gulf.getTrackByName("Gulf Yard 2", null);
        Assert.assertEquals("Comment", "Comment for gulf yard 2", gulfYard2.getComment());
        Assert.assertEquals("Setout Comment", "Setout comment for gulf yard 2", gulfYard2.getCommentSetout());
    }

    @Test
    public void verifyFields() {
        Assert.assertEquals("FIELD_LOCATION", 0, ImportLocations.FIELD_LOCATION);
        Assert.assertEquals("FIELD_DIVISION", 8, ImportLocations.FIELD_DIVISION);
        Assert.assertEquals("FIELD_COMMENT_SETOUTS", 45, ImportLocations.FIELD_COMMENT_SETOUTS);
    }
}
