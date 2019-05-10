package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.GraphicsEnvironment;
import java.io.File;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.junit.rules.*;
import jmri.util.swing.JemmyUtil;

import org.junit.*;
import org.junit.rules.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ImportEnginesTest extends OperationsTestCase {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(60); // 60 second timeout for methods in this test class.

    @Rule
    public RetryRule retryRule = new RetryRule(2); // allow 2 retries

    @Test
    public void testCTor() {
        ImportEngines t = new ImportEngines();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testReadFile() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        JUnitOperationsUtil.initOperationsData();
        // check number of engines in operations data
        Assert.assertEquals("engines", 4, emanager.getNumEntries());

        // export engines to create file
        ExportEngines exportEngines = new ExportEngines();
        Assert.assertNotNull("exists", exportEngines);

        // should cause export complete dialog to appear
        Thread export = new Thread(new Runnable() {
            @Override
            public void run() {
                exportEngines.writeOperationsEngineFile();
            }
        });
        export.setName("Export Engines"); // NOI18N
        export.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return export.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), Bundle.getMessage("ButtonOK"));
        
        try {
            export.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        java.io.File file = new java.io.File(ExportEngines.defaultOperationsFilename());
        Assert.assertTrue("Confirm file creation", file.exists());

        // delete all engines
        emanager.deleteAll();
        Assert.assertEquals("engines", 0, emanager.getNumEntries());

        // do import      
        Thread mb = new ImportEngines(){
            @Override
            protected File getFile() {
                // replace JFileChooser with fixed file to avoid threading issues
                return new File(OperationsXml.getFileLocation()+OperationsXml.getOperationsDirectoryName() + File.separator + ExportEngines.getOperationsFileName());
            }
        };
        mb.setName("Test Import Engines"); // NOI18N
        mb.start();
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return mb.getState().equals(Thread.State.WAITING);
        }, "wait for dialog");

        // dialog windows should now open asking to add 2 models
        JemmyUtil.pressDialogButton(Bundle.getMessage("engineAddModel"), Bundle.getMessage("ButtonYes"));
       
        jmri.util.JUnitUtil.waitFor(() -> {
            return mb.getState().equals(Thread.State.WAITING);
        }, "wait for dialog");
        
        JemmyUtil.pressDialogButton(Bundle.getMessage("engineAddModel"), Bundle.getMessage("ButtonYes"));
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return mb.getState().equals(Thread.State.WAITING);
        }, "wait for import to finish");

        // import complete 
        JemmyUtil.pressDialogButton(Bundle.getMessage("SuccessfulImport"), Bundle.getMessage("ButtonOK"));

        try {
            mb.join();
        } catch (InterruptedException e) {
            // do nothing
        }
        // confirm import successful
        Assert.assertEquals("engines", 4, emanager.getNumEntries());
    }

    @Test
    public void testImportEnginesWithLocations() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        JUnitOperationsUtil.initOperationsData();
        // check number of engines in operations data
        Assert.assertEquals("engines", 4, emanager.getNumEntries());

        // give an engine a location and track assignment
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Track track = loc.getTrackByName("NI Yard", null);

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        Assert.assertEquals("place engine on tracck", Track.OKAY, e1.setLocation(loc, track));

        // export engines to create file
        ExportEngines exportEngines = new ExportEngines();
        Assert.assertNotNull("exists", exportEngines);

        // should cause export complete dialog to appear
        Thread export = new Thread(new Runnable() {
            @Override
            public void run() {
                exportEngines.writeOperationsEngineFile();
            }
        });
        export.setName("Export Engines"); // NOI18N
        export.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return export.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), Bundle.getMessage("ButtonOK"));
        
        try {
            export.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        java.io.File file = new java.io.File(ExportEngines.defaultOperationsFilename());
        Assert.assertTrue("Confirm file creation", file.exists());

        // delete all engines
        emanager.deleteAll();
        Assert.assertEquals("engines", 0, emanager.getNumEntries());
        // delete location
        lmanager.deregister(loc);

        // do import      
        Thread mb = new ImportEngines(){
            @Override
            protected File getFile() {
                // replace JFileChooser with fixed file to avoid threading issues
                return new File(OperationsXml.getFileLocation()+OperationsXml.getOperationsDirectoryName() + File.separator + ExportEngines.getOperationsFileName());
            }
        };
        mb.setName("Test Import Engines"); // NOI18N
        mb.start();

        // dialog windows should now open asking to add 2 models
        JemmyUtil.pressDialogButton(Bundle.getMessage("engineAddModel"), Bundle.getMessage("ButtonYes"));
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return mb.getState().equals(Thread.State.WAITING);
        }, "wait for dialog");
        
        JemmyUtil.pressDialogButton(Bundle.getMessage("engineAddModel"), Bundle.getMessage("ButtonYes"));

        jmri.util.JUnitUtil.waitFor(() -> {
            return mb.getState().equals(Thread.State.WAITING);
        }, "wait for dialog");
        
        // new dialog window should open stating that location doesn't exist
        JemmyUtil.pressDialogButton(Bundle.getMessage("engineLocation"), Bundle.getMessage("ButtonOK"));
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return mb.getState().equals(Thread.State.WAITING);
        }, "wait for dialog");
        
        // create location
        JemmyUtil.pressDialogButton(Bundle.getMessage("engineLocation"), Bundle.getMessage("ButtonYes"));
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return mb.getState().equals(Thread.State.WAITING);
        }, "wait for dialog");
        
        // new dialog window should open stating that location doesn't exist
        JemmyUtil.pressDialogButton(Bundle.getMessage("engineTrack"), Bundle.getMessage("ButtonOK"));
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return mb.getState().equals(Thread.State.WAITING);
        }, "wait for dialog");
        
        // create track
        JemmyUtil.pressDialogButton(Bundle.getMessage("engineTrack"), Bundle.getMessage("ButtonYes"));

        jmri.util.JUnitUtil.waitFor(() -> {
            return mb.getState().equals(Thread.State.WAITING);
        }, "wait for dialog");
        
        // import complete 
        JemmyUtil.pressDialogButton(Bundle.getMessage("SuccessfulImport"), Bundle.getMessage("ButtonOK"));

        try {
            mb.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        // confirm import successful
        Assert.assertEquals("engines", 4, emanager.getNumEntries());
    }

    // private final static Logger log = LoggerFactory.getLogger(ImportEnginesTest.class);

}
