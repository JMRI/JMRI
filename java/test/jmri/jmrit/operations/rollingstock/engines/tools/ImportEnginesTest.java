package jmri.jmrit.operations.rollingstock.engines.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@Timeout(60)
public class ImportEnginesTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ImportEngines t = new ImportEngines();
        assertThat(t).withFailMessage("exists").isNotNull();
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @DisabledIfSystemProperty(named = "jmri.skipjythontests", matches = "true")
    public void testReadFile() {
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        JUnitOperationsUtil.initOperationsData();
        // check number of engines in operations data
        assertThat(emanager.getNumEntries()).withFailMessage("engines").isEqualTo(4);

        // export engines to create file
        ExportEngines exportEngines = new ExportEngines();
        assertThat(exportEngines).withFailMessage("exists").isNotNull();

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
        assertThat(file.exists()).withFailMessage("Confirm file creation").isTrue();

        // delete all engines
        emanager.deleteAll();
        assertThat(emanager.getNumEntries()).withFailMessage("engines").isEqualTo(0);

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
        assertThat(emanager.getNumEntries()).withFailMessage("engines").isEqualTo(4);
        

    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testImportEnginesWithLocations() {
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        JUnitOperationsUtil.initOperationsData();
        // check number of engines in operations data
        assertThat(emanager.getNumEntries()).withFailMessage("engines").isEqualTo(4);

        // give an engine a location and track assignment
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Track track = loc.getTrackByName("NI Yard", null);

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        assertThat(e1.setLocation(loc, track)).withFailMessage("place engine on tracck").isEqualTo(Track.OKAY);

        // export engines to create file
        ExportEngines exportEngines = new ExportEngines();
        assertThat(exportEngines).withFailMessage("exists").isNotNull();

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
        assertThat(file.exists()).withFailMessage("Confirm file creation").isTrue();

        // delete all engines
        emanager.deleteAll();
        assertThat(emanager.getNumEntries()).withFailMessage("engines").isEqualTo(0);
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
        assertThat(emanager.getNumEntries()).withFailMessage("engines").isEqualTo(4);
        

    }

    // private final static Logger log = LoggerFactory.getLogger(ImportEnginesTest.class);

}
