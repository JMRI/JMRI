package jmri.jmrit.operations.rollingstock.engines.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.rollingstock.engines.*;
import jmri.jmrit.operations.setup.Control;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;

/**
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
@Timeout(60)
public class ImportEnginesTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ImportEngines t = new ImportEngines();
        assertThat(t).withFailMessage("exists").isNotNull();
    }

    @Test
    public void testImportEngineNoEngineNumber() {
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        addEngineModels();

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        e1.setNumber("");
        exportEngines();
        importEngines(true, Bundle.getMessage("RoadNumberMissing"), Bundle.getMessage("ButtonOK"));

        // confirm failure
        Assert.assertEquals("Engines", 3, emanager.getNumEntries());
    }

    @Test
    public void testImportEngineEngineNumberTooLong() {
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        addEngineModels();

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        e1.setNumber("12345678901");
        exportEngines();
        importEngines(true, Bundle.getMessage("RoadNumMustBeLess"), Bundle.getMessage("ButtonOK"));

        // confirm failure
        Assert.assertEquals("Engines", 3, emanager.getNumEntries());
    }

    @Test
    public void testImportEngineNoEngineRoadName() {
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        addEngineModels();

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        e1.setRoadName("");
        exportEngines();
        importEngines(true, Bundle.getMessage("RoadNameMissing"), Bundle.getMessage("ButtonOK"));

        // confirm failure
        Assert.assertEquals("Engines", 3, emanager.getNumEntries());
    }

    @Test
    public void testImportEngineRoadNameTooLong() {
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        addEngineModels();

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        e1.setRoadName("ABCDEFGHIJKLM");
        exportEngines();
        importEngines(true, MessageFormat.format(Bundle.getMessage("engineAttribute"),
                new Object[]{Control.max_len_string_attibute}), Bundle.getMessage("ButtonOK"));

        // confirm failure
        Assert.assertEquals("Engines", 3, emanager.getNumEntries());
    }

    @Test
    public void testImportEngineNoEngineModel() {
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        addEngineModels();

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        e1.setModel("");
        exportEngines();
        importEngines(true, Bundle.getMessage("EngineModelMissing"), Bundle.getMessage("ButtonOK"));

        // confirm failure
        Assert.assertEquals("Engines", 3, emanager.getNumEntries());
    }

    @Test
    public void testImportEngineModelTooLong() {
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        addEngineModels();

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        e1.setModel("ABCDEFGHIJKLM");
        e1.setLength("60"); // new model needs a length
        exportEngines();
        importEngines(true, MessageFormat.format(Bundle.getMessage("engineAttribute"),
                new Object[]{Control.max_len_string_attibute}), Bundle.getMessage("ButtonOK"));

        // confirm failure
        Assert.assertEquals("Engines", 3, emanager.getNumEntries());
    }

    @Test
    public void testImportEngineNoLength() {
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        addEngineModels();

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        // changes the length of the 2 engines with this model name
        e1.setLength("");
        exportEngines();
        importEngines(true, Bundle.getMessage("EngineLengthMissing"), Bundle.getMessage("ButtonOK"));

        // confirm failure
        Assert.assertEquals("Engines", 2, emanager.getNumEntries());
    }

    @Test
    public void testImportEngineLengthTooLong() {
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        addEngineModels();

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        // both engines get modified
        e1.setLength("ABCDE");
        exportEngines();
        importEngines(true, MessageFormat.format(Bundle.getMessage("engineAttribute"),
                new Object[]{Control.max_len_string_length_name}), Bundle.getMessage("ButtonOK"));

        // confirm failure
        Assert.assertEquals("Engines", 2, emanager.getNumEntries());
    }

    @Test
    public void testImportEngineLengthNotNumber() {
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        addEngineModels();

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        // both engines get modified
        e1.setLength("ABCD");
        exportEngines();
        importEngines(true, Bundle.getMessage("EngineLengthMissing"), Bundle.getMessage("ButtonOK"));

        // confirm failure
        Assert.assertEquals("Engines", 2, emanager.getNumEntries());
    }

    @Test
    public void testImportEngineOwnerNameTooLong() {
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        addEngineModels();

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        e1.setOwnerName("ABCDEFGHIJKLM");
        exportEngines();
        importEngines(true, MessageFormat.format(Bundle.getMessage("engineAttribute"),
                new Object[]{Control.max_len_string_attibute}), Bundle.getMessage("ButtonOK"));

        // confirm failure
        Assert.assertEquals("Engines", 3, emanager.getNumEntries());
    }

    @Test
    public void testImportEngineBuiltDateTooLong() {
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        addEngineModels();

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        e1.setBuilt("ABCDEF");
        exportEngines();
        importEngines(true, MessageFormat.format(Bundle.getMessage("engineAttribute"),
                new Object[]{Control.max_len_string_built_name}), Bundle.getMessage("ButtonOK"));

        // confirm failure
        Assert.assertEquals("Engines", 3, emanager.getNumEntries());
    }

    @Test
    public void testImportEngineLocationNameTooLong() {
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        addEngineModels();

        // give the engine a location and track assignment
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Track track = loc.getTrackByName("NI Yard", null);

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        e1.setLocation(loc, track);
        loc.setName("ABCDEFGHIJKLMNOPQRSTUVWXYZA");
        exportEngines();
        importEngines(true, MessageFormat.format(Bundle.getMessage("engineAttribute"),
                new Object[]{Control.max_len_string_location_name}), Bundle.getMessage("ButtonOK"));

        // confirm failure
        Assert.assertEquals("Engines", 3, emanager.getNumEntries());
    }

    @Test
    public void testImportEngineTrackNameTooLong() {
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        addEngineModels();

        // give the engine a location and track assignment
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Track track = loc.getTrackByName("NI Yard", null);

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        e1.setLocation(loc, track);
        track.setName("ABCDEFGHIJKLMNOPQRSTUVWXYZA");
        exportEngines();
        importEngines(true, MessageFormat.format(Bundle.getMessage("engineAttribute"),
                new Object[]{Control.max_len_string_track_name}), Bundle.getMessage("ButtonOK"));

        // confirm failure
        Assert.assertEquals("Engines", 3, emanager.getNumEntries());
    }

    @Test
    public void testImportNewModelCreation() {
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        addEngineModels();

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        e1.setModel("NewModel");
        e1.setLength("60"); // new model needs a length
        // for test coverage
        e1.setConsist(null);

        exportEngines();
        importEngines(false, Bundle.getMessage("engineAddModel"), Bundle.getMessage("ButtonYes"));

        // confirm success
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        e1 = emanager.getByRoadAndNumber("PC", "5559");
        Assert.assertEquals("default type", Bundle.getMessage("engineDefaultType"), e1.getTypeName());
        Assert.assertEquals("default HP", Bundle.getMessage("engineDefaultHp"), e1.getHp());
        Assert.assertEquals("consist", null, e1.getConsist());
    }

    @Test
    public void testImportNewModelCreation2() {
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        addEngineModels();

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        e1.setModel("NewModel");
        e1.setLength("60"); // new model needs a length
        e1.setTypeName("New Type");
        e1.setHp("2345");

        exportEngines();

        // eliminate the NewModel from the database
        EngineModels em = InstanceManager.getDefault(EngineModels.class);
        em.setModelType("NewModel", "");
        em.setModelHorsepower("NewModel", "");
        em.deleteName("NewModel");

        importEngines(false, Bundle.getMessage("engineAddModel"), Bundle.getMessage("ButtonYes"));

        // confirm success
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        e1 = emanager.getByRoadAndNumber("PC", "5559");
        Assert.assertEquals("type", "New Type", e1.getTypeName());
        Assert.assertEquals("HP", "2345", e1.getHp());
        Assert.assertEquals("consist", "C14", e1.getConsistName());
    }

    @Test
    public void testImportAddExistingEngine() {
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        addEngineModels();

        exportEngines();
        emanager.newRS("PC", "5559");
        Assert.assertEquals("Engines", 1, emanager.getNumEntries());

        importEngines(false, null, null);

        // confirm success
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
    }

    @Test
    public void testImportTrackTooShortYes() {
        JUnitOperationsUtil.initOperationsData();
        // check number of engines in operations data
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        assertThat(emanager.getNumEntries()).withFailMessage("engines").isEqualTo(4);

        // give an engine a location and track assignment
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Track track = loc.getTrackByName("NI Yard", null);

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        assertThat(e1.setLocation(loc, track)).withFailMessage("place engine on track").isEqualTo(Track.OKAY);
        addEngineModels();

        // export engines to create file
        exportEngines();
        
        // make track too short for 2 engines
        track.setLength(70);
        importEngines(false, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"),
                Bundle.getMessage("TrackLength"), Bundle.getMessage("ButtonYes"), null, null);
        
        // confirm success
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        Assert.assertEquals("track length", 1070, track.getLength());
    }
    
    @Test
    public void testImportTrackTypeYes() {
        JUnitOperationsUtil.initOperationsData();
        // check number of engines in operations data
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        assertThat(emanager.getNumEntries()).withFailMessage("engines").isEqualTo(4);

        // give an engine a location and track assignment
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Track track = loc.getTrackByName("NI Yard", null);

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        assertThat(e1.setLocation(loc, track)).withFailMessage("place engine on track").isEqualTo(Track.OKAY);
        addEngineModels();

        // export engines to create file
        exportEngines();
        
        track.deleteTypeName("Diesel");
        importEngines(false, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"),
                Bundle.getMessage("ServiceEngineType"), Bundle.getMessage("ButtonYes"), null, null);
        
        // confirm success
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
    }
    
    @Test
    public void testImportTrackCapacityYes() {
        JUnitOperationsUtil.initOperationsData();
        // check number of engines in operations data
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        assertThat(emanager.getNumEntries()).withFailMessage("engines").isEqualTo(4);

        // give an engine a location and track assignment
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Track track = loc.getTrackByName("NI Yard", null);

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        assertThat(e1.setLocation(loc, track)).withFailMessage("place engine on track").isEqualTo(Track.OKAY);
        addEngineModels();

        // export engines to create file
        exportEngines();
        
        // make track too short, now a capacity issue
        track.setLength(0);
        importEngines(false, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"),
                Bundle.getMessage("TrackLength"), Bundle.getMessage("ButtonYes"), null, null);
        
        // confirm success
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
        Assert.assertEquals("track length", 1000, track.getLength());
    }
    
    @Test
    public void testImportTrackForceYes() {
        JUnitOperationsUtil.initOperationsData();
        // check number of engines in operations data
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        assertThat(emanager.getNumEntries()).withFailMessage("engines").isEqualTo(4);

        // give an engine a location and track assignment
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Track track = loc.getTrackByName("NI Yard", null);

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        assertThat(e1.setLocation(loc, track)).withFailMessage("place engine on track").isEqualTo(Track.OKAY);
        addEngineModels();

        // export engines to create file
        exportEngines();
        
        // don't allow road PC
        track.setRoadOption(Track.EXCLUDE_ROADS);
        track.addRoadName("PC");
        importEngines(false, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"),
                Bundle.getMessage("OverRide"), Bundle.getMessage("ButtonYes"), null, null);
        
        // confirm success
        Assert.assertEquals("Engines", 4, emanager.getNumEntries());
    }


    @Test
    @DisabledIfSystemProperty(named = "jmri.skipjythontests", matches = "true")
    public void testReadFile() {
        JUnitOperationsUtil.initOperationsData();
        // check number of engines in operations data
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        assertThat(emanager.getNumEntries()).withFailMessage("engines").isEqualTo(4);

        // export engines to create file
        exportEngines();

        // do import
        Thread mb = new ImportEngines() {
            @Override
            protected File getFile() {
                // replace JFileChooser with fixed file to avoid threading
                // issues
                return new File(OperationsXml.getFileLocation() +
                        OperationsXml.getOperationsDirectoryName() +
                        File.separator +
                        ExportEngines.getOperationsFileName());
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
    public void testImportEnginesWithLocations() {
        JUnitOperationsUtil.initOperationsData();
        // check number of engines in operations data
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        assertThat(emanager.getNumEntries()).withFailMessage("engines").isEqualTo(4);

        // give an engine a location and track assignment
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Track track = loc.getTrackByName("NI Yard", null);

        Engine e1 = emanager.getByRoadAndNumber("PC", "5559");
        assertThat(e1.setLocation(loc, track)).withFailMessage("place engine on tracck").isEqualTo(Track.OKAY);

        // export engines to create file
        exportEngines();

        // delete location
        lmanager.deregister(loc);

        // do import
        Thread mb = new ImportEngines() {
            @Override
            protected File getFile() {
                // replace JFileChooser with fixed file to avoid threading
                // issues
                return new File(OperationsXml.getFileLocation() +
                        OperationsXml.getOperationsDirectoryName() +
                        File.separator +
                        ExportEngines.getOperationsFileName());
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

    private void addEngineModels() {
        // add the models to eliminate dialogs on import
        EngineModels em = InstanceManager.getDefault(EngineModels.class);
        em.addName("GP40");
        em.addName("SD45");
    }

    /*
     * Exports engines and then removes all of them from the roster.
     */
    private void exportEngines() {
        // export engines to create file
        EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);
        ExportEngines exportEngines = new ExportEngines(engineManager.getByNumberList());
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
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        emanager.deleteAll();
        assertThat(emanager.getNumEntries()).withFailMessage("engines").isEqualTo(0);
    }

    private void importEngines(boolean failure, String title1, String button1) {
        importEngines(failure, title1, button1, null, null, null, null);
    }

    /*
     * title of error dialog, button name.
     */
    private void importEngines(boolean failure, String title1, String button1, String title2, String button2,
            String title3, String button3) {
        Thread mb = new ImportEngines() {
            @Override
            protected File getFile() {
                // replace JFileChooser with fixed file to avoid threading
                // issues
                return new File(OperationsXml.getFileLocation() +
                        OperationsXml.getOperationsDirectoryName() +
                        File.separator +
                        ExportEngines.getOperationsFileName());
            }
        };
        mb.setName("Test Import Engines"); // NOI18N
        mb.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return mb.getState().equals(Thread.State.WAITING);
        }, "wait for dialog");

        // up to 3 dialog windows can appear
        if (title1 != null) {
            JemmyUtil.pressDialogButton(title1, button1);
        }
        if (title2 != null) {
            JemmyUtil.pressDialogButton(title2, button2);
        }
        if (title3 != null) {
            JemmyUtil.pressDialogButton(title3, button3);
        }

        if (failure) {
            // wait for import failed
            JemmyUtil.pressDialogButton(Bundle.getMessage("ImportFailed"), Bundle.getMessage("ButtonOK"));
        } else {
            // wait for import complete and acknowledge
            JemmyUtil.pressDialogButton(Bundle.getMessage("SuccessfulImport"), Bundle.getMessage("ButtonOK"));
        }

        try {
            mb.join();
        } catch (InterruptedException e) {
            // do nothing
        }
    }
}
