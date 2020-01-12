package jmri.jmrit.operations.rollingstock.engines;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.engines.tools.EngineAttributeEditFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import jmri.util.junit.rules.*;
import org.junit.*;
import org.junit.rules.*;

/**
 * Tests for the Operations EngineEditFrame class
 *
 * @author	Dan Boudreau Copyright (C) 2010
 *
 */
public class EngineEditFrameTest extends OperationsTestCase {
    
    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 second timeout for methods in this test class.

    @Rule
    public RetryRule retryRule = new RetryRule(2); // allow 2 retries      

//    List<String> tempEngines;
    
    @Test
    public void testClearRoadNumber() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();
        Assert.assertTrue(f.isShowing());

        f.roadNumberTextField.setText("123");
        JemmyUtil.enterClickAndLeave(f.clearRoadNumberButton);
        Assert.assertEquals("road number", "", f.roadNumberTextField.getText());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testRoadNumberErrorConditionsAddEngine() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();
        Assert.assertTrue(f.isShowing());

        // this will load the weight fields
        f.lengthComboBox.setSelectedIndex(4);

        // "*" is not a legal character for road number
        f.roadNumberTextField.setText("6*6");

        JemmyUtil.enterClickAndLeave(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("roadNumNG"), Bundle.getMessage("ButtonOK"));

        // test number too long
        StringBuffer sb = new StringBuffer("A");
        for (int i = 0; i < Control.max_len_string_road_number; i++) {
            sb.append(i);
        }

        f.roadNumberTextField.setText(sb.toString());

        JemmyUtil.enterClickAndLeave(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("RoadNumTooLong"), Bundle.getMessage("ButtonOK"));

        // confirm that delete and save buttons are disabled
        Assert.assertFalse(f.saveButton.isEnabled());
        Assert.assertFalse(f.deleteButton.isEnabled());

        // enter a good road number
        f.roadNumberTextField.setText("123");

        JemmyUtil.enterClickAndLeave(f.addButton);
        // confirm that delete and save buttons are enabled
        Assert.assertTrue(f.saveButton.isEnabled());
        Assert.assertTrue(f.deleteButton.isEnabled());

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testRoadNumberErrorConditionsSaveEngine() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        
        EngineManager cManager = InstanceManager.getDefault(EngineManager.class);
        Engine e1 = cManager.getByRoadAndNumber("PC", "5016");
        
        EngineLengths el = InstanceManager.getDefault(EngineLengths.class);
        el.addName("59");

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();
        f.load(e1);
        Assert.assertTrue(f.isShowing());
        
        // confirm that delete and save buttons are enabled
        Assert.assertTrue(f.saveButton.isEnabled());
        Assert.assertTrue(f.deleteButton.isEnabled());

        // "*" is not a legal character for road number
        f.roadNumberTextField.setText("6*6");

        JemmyUtil.enterClickAndLeave(f.saveButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("roadNumNG"), Bundle.getMessage("ButtonOK"));

        // test number too long
        StringBuffer sb = new StringBuffer("A");
        for (int i = 0; i < Control.max_len_string_road_number; i++) {
            sb.append(i);
        }

        f.roadNumberTextField.setText(sb.toString());

        JemmyUtil.enterClickAndLeave(f.saveButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("RoadNumTooLong"), Bundle.getMessage("ButtonOK"));

        // enter a good road number
        f.roadNumberTextField.setText("123");

        JemmyUtil.enterClickAndLeave(f.saveButton);
        
        Assert.assertNull(cManager.getByRoadAndNumber("PC", "5016"));
        Assert.assertNotNull(cManager.getByRoadAndNumber("PC", "123"));

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testSaveEngine() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();

        // confirm load
        EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("number of engines", 4, engineManager.getNumEntries());

        Engine engine = engineManager.getByRoadAndNumber("PC", "5559");
        // confirm engine id
        Assert.assertEquals("engine id", "PC5559", engine.getId());

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();
        f.load(engine);

        Assert.assertEquals("engine road", "PC", f.roadComboBox.getSelectedItem());
        Assert.assertEquals("engine number", "5559", f.roadNumberTextField.getText());

        // change road number for this engine
        f.roadNumberTextField.setText("54321");
        JemmyUtil.enterClickAndLeave(f.saveButton);
        engine = engineManager.getByRoadAndNumber("PC", "54321");
        Assert.assertNotNull("engine exists", engine);
        // confirm engine id was modified
        Assert.assertEquals("engine id", "PC54321", engine.getId());

        // close on save
        Setup.setCloseWindowOnSaveEnabled(true);

        // change road name
        f.roadComboBox.setSelectedItem("SP");
        JemmyUtil.enterClickAndLeave(f.saveButton);
        engine = engineManager.getByRoadAndNumber("SP", "54321");
        Assert.assertNotNull("engine exists", engine);
        // confirm engine id was modified
        Assert.assertEquals("engine id", "SP54321", engine.getId());

        Assert.assertFalse("window closed", f.isVisible());
    }
    
    @Test
    public void testSaveExistingEngine() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        
        EngineManager cManager = InstanceManager.getDefault(EngineManager.class);
        Engine e1 = cManager.getByRoadAndNumber("PC", "5016");
        
        EngineLengths el = InstanceManager.getDefault(EngineLengths.class);
        el.addName("59");

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();
        f.load(e1);
        Assert.assertTrue(f.isShowing());
        
        // confirm that 5019 already exists
        Assert.assertNotNull(cManager.getByRoadAndNumber("PC", "5019"));

        // enter an existing road number
        f.roadNumberTextField.setText("5019");

        JemmyUtil.enterClickAndLeave(f.saveButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("engineCanNotUpdate"), Bundle.getMessage("ButtonOK"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testWeightErrorConditions() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();
        Assert.assertTrue(f.isShowing());
        
        // confirm that delete and save buttons are NOT enabled
        Assert.assertFalse(f.saveButton.isEnabled());
        Assert.assertFalse(f.deleteButton.isEnabled());

        // enter a good road number
        f.roadNumberTextField.setText("123456");
  
        f.weightTonsTextField.setText("Bogus Weight");
        // new dialog warning engine weight
        JemmyUtil.enterClickAndLeave(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("WeightTonError"), Bundle.getMessage("ButtonOK"));

        f.weightTonsTextField.setText("100");
        JemmyUtil.enterClickAndLeave(f.addButton);

        // confirm that delete and save buttons are enabled
        Assert.assertTrue(f.saveButton.isEnabled());
        Assert.assertTrue(f.deleteButton.isEnabled());

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testHpErrorConditions() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();
        Assert.assertTrue(f.isShowing());
  
        f.hpTextField.setText("Bogus HP");
        // new dialog warning engine HP
        JemmyUtil.enterClickAndLeave(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("engineCanNotHp"), Bundle.getMessage("ButtonOK"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testEditRoadButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editRoadButton);
        Assert.assertTrue(f.engineAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", EngineAttributeEditFrame.ROAD, f.engineAttributeEditFrame._comboboxName);
        
        // test that the attribute edit frame gets disposed
        f.buttonEditActionPerformed(new ActionEvent("null", 0, null));
        Assert.assertFalse(f.engineAttributeEditFrame.isShowing());

        JUnitUtil.dispose(f.engineAttributeEditFrame);
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testEditModelButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editModelButton);
        Assert.assertTrue(f.engineAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", EngineAttributeEditFrame.MODEL, f.engineAttributeEditFrame._comboboxName);

        JUnitUtil.dispose(f.engineAttributeEditFrame);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testEditTypeButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editTypeButton);
        Assert.assertTrue(f.engineAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", EngineAttributeEditFrame.TYPE, f.engineAttributeEditFrame._comboboxName);

        JUnitUtil.dispose(f.engineAttributeEditFrame);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testEditLengthButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editLengthButton);
        Assert.assertTrue(f.engineAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", EngineAttributeEditFrame.LENGTH, f.engineAttributeEditFrame._comboboxName);

        JUnitUtil.dispose(f.engineAttributeEditFrame);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testEditOwnerButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editOwnerButton);
        Assert.assertTrue(f.engineAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", EngineAttributeEditFrame.OWNER, f.engineAttributeEditFrame._comboboxName);

        JUnitUtil.dispose(f.engineAttributeEditFrame);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testEditGroupButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editGroupButton);
        Assert.assertTrue(f.engineAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", EngineAttributeEditFrame.CONSIST, f.engineAttributeEditFrame._comboboxName);

        JUnitUtil.dispose(f.engineAttributeEditFrame);
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testLocationComboBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();

        LocationManager lm = InstanceManager.getDefault(LocationManager.class);
        Location loc1 = lm.getLocationByName("North End Staging");
        Location loc2 = lm.getLocationByName("North Industries");

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();
        Assert.assertTrue(f.isShowing());

        f.roadNumberTextField.setText("10345");
        f.roadComboBox.setSelectedItem("SP");

        EngineManager cm = InstanceManager.getDefault(EngineManager.class);
        Engine engine = cm.getByRoadAndNumber("SP", "10345");
        Assert.assertNull("engine exists", engine);

        f.lengthComboBox.setSelectedIndex(4);

        // test no track selected error
        f.locationBox.setSelectedIndex(1);
        JemmyUtil.enterClickAndLeave(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"));

        engine = cm.getByRoadAndNumber("SP", "10345");
        Assert.assertNotNull("engine exists", engine);

        Assert.assertEquals("engine location", null, engine.getLocation());

        f.trackLocationBox.setSelectedIndex(1);
        JemmyUtil.enterClickAndLeave(f.addButton);

        // engine already exists
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("engineCanNotUpdate"), Bundle.getMessage("ButtonOK"));
        Assert.assertEquals("engine location", null, engine.getLocation());

        JemmyUtil.enterClickAndLeave(f.saveButton);
        // engine location should have been updated
        Assert.assertEquals("engine location", loc1, engine.getLocation());

        // now change location
        f.locationBox.setSelectedIndex(2);
        f.trackLocationBox.setSelectedIndex(1);

        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertEquals("engine location", loc2, engine.getLocation());

        // add a track
        Assert.assertEquals("Number of locations", 4, f.locationBox.getItemCount());
        Assert.assertEquals("Number of tracks", 2, f.trackLocationBox.getItemCount());
        Track testSpur = loc2.addTrack("Test_Spur", Track.SPUR);

        // add a location to cause update to both location and track comboboxes
        lm.newLocation("Test_Location");
        Assert.assertEquals("Number of locations", 5, f.locationBox.getItemCount());
        Assert.assertEquals("Number of tracks", 3, f.trackLocationBox.getItemCount());

        // try to set engine to test spur, with a length of 0
        f.trackLocationBox.setSelectedIndex(2);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        JemmyUtil.pressDialogButton(f, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"));

        // get response message
        String status = engine.setLocation(loc2, testSpur);
        Assert.assertFalse(status.equals(Track.OKAY));
        JemmyUtil.pressDialogButton(f, MessageFormat
                .format(Bundle.getMessage("rsOverride"), new Object[]{status}), Bundle.getMessage("ButtonNo"));

        // confirm engine location and track didn't change
        Assert.assertNotEquals("track", testSpur, engine.getTrack());

        // do it again, but say yes
        JemmyUtil.enterClickAndLeave(f.saveButton);

        JemmyUtil.pressDialogButton(f, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.pressDialogButton(f, MessageFormat
                .format(Bundle.getMessage("rsOverride"), new Object[]{status}), Bundle.getMessage("ButtonYes"));

        // confirm engine location and track changed
        Assert.assertEquals("track", testSpur, engine.getTrack());

        JUnitUtil.dispose(f);
    }


    @Test
    public void testAddEngine() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        
        // increase test coverage
        Setup.setValueEnabled(true);
        Setup.setRfidEnabled(true);
        
        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();
        f.setTitle("Test Add Engine Frame");

        // add a new Engine
        f.roadComboBox.setSelectedItem("SP");
        f.roadNumberTextField.setText("6");
        f.modelComboBox.setSelectedItem("SW8");
        f.builtTextField.setText("1999");
        f.ownerComboBox.setSelectedItem("AT");
        f.commentTextField.setText("test Engine comment field");
        JemmyUtil.enterClickAndLeave(f.addButton);

        EngineManager cManager = InstanceManager.getDefault(EngineManager.class);
        Assert.assertEquals("number of Engines", 5, cManager.getNumEntries());
        Engine e6 = cManager.getByRoadAndNumber("SP", "6");

        Assert.assertNotNull("Engine exists", e6);
        Assert.assertEquals("Engine type", "SW8", e6.getModel());
        Assert.assertEquals("Engine type", "Diesel", e6.getTypeName());
        Assert.assertEquals("Engine length", "44", e6.getLength()); //default for SW8 is 44
        Assert.assertEquals("Engine built", "1999", e6.getBuilt());
        Assert.assertEquals("Engine owner", "AT", e6.getOwner());
        Assert.assertEquals("Engine comment", "test Engine comment field", e6.getComment());
        Assert.assertFalse(e6.isBunit());
        Assert.assertEquals("Blocking order", Engine.DEFAULT_BLOCKING_ORDER, e6.getBlocking());
        
        // make B unit
        JemmyUtil.enterClickAndLeave(f.bUnitCheckBox);

        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertEquals("number of Engines", 5, cManager.getNumEntries());
        
        Assert.assertTrue(e6.isBunit());
        Assert.assertEquals("Blocking order", Engine.B_UNIT_BLOCKING, e6.getBlocking());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testEngineEditFrameRead() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        
        EngineManager cManager = InstanceManager.getDefault(EngineManager.class);
        Engine e1 = cManager.getByRoadAndNumber("PC", "5016");
        e1.setComment("Test engine Comment for E1");
        
        EngineLengths el = InstanceManager.getDefault(EngineLengths.class);
        el.addName("59");
        
        Location location = InstanceManager.getDefault(LocationManager.class).getLocationByName("North End Staging");
        Track track = location.getTrackByName("North End 1", null);
        Assert.assertEquals("place engine", Track.OKAY, e1.setLocation(location, track));

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();
        f.load(e1);
        f.setTitle("Test Edit Engine Frame");

        Assert.assertEquals("Engine road", "PC", f.roadComboBox.getSelectedItem());
        Assert.assertEquals("Engine number", "5016", f.roadNumberTextField.getText());
        Assert.assertEquals("Engine type", "GP40", f.modelComboBox.getSelectedItem());
        Assert.assertEquals("Engine type", "Diesel", f.typeComboBox.getSelectedItem());
        Assert.assertEquals("Engine length", "59", f.lengthComboBox.getSelectedItem());
        Assert.assertEquals("Engine weight", "122", f.weightTonsTextField.getText());
        Assert.assertEquals("Engine built", "1990", f.builtTextField.getText());
        Assert.assertEquals("Engine owner", "AT", f.ownerComboBox.getSelectedItem());
        Assert.assertEquals("Engine comment", "Test engine Comment for E1", f.commentTextField.getText());
        
        Assert.assertEquals("Engine location", location, f.locationBox.getSelectedItem());
        Assert.assertEquals("Engine track", track, f.trackLocationBox.getSelectedItem());

        // test delete button
        JemmyUtil.enterClickAndLeave(f.deleteButton);
        Assert.assertEquals("number of Engines", 3, cManager.getNumEntries());

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testAddNewRoad() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData(); // load engines
        EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);

        Engine e1 = engineManager.getByRoadAndNumber("PC", "5524");
        e1.setRoadName("TEST_ROAD");

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();

        // should cause add road dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(e1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addRoad"), Bundle.getMessage("ButtonNo"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertFalse(InstanceManager.getDefault(CarRoads.class).containsName("TEST_ROAD"));

        // now answer yes to add road
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(e1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addRoad"), Bundle.getMessage("ButtonYes"));

        try {
            load2.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertTrue(InstanceManager.getDefault(CarRoads.class).containsName("TEST_ROAD"));

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testAddNewModel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData(); // load engines
        EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);

        Engine e1 = engineManager.getByRoadAndNumber("PC", "5524");
        e1.setModel("TEST_MODEL");
        e1.setTypeName("Diesel");
        e1.setLength("50");
        e1.setHp("2000");

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();

        // should cause add model dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(e1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("engineAddModel"), Bundle.getMessage("ButtonNo"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertFalse(InstanceManager.getDefault(EngineModels.class).containsName("TEST_MODEL"));

        // now answer yes to add model
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(e1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("engineAddModel"), Bundle.getMessage("ButtonYes"));

        try {
            load2.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertTrue(InstanceManager.getDefault(EngineModels.class).containsName("TEST_MODEL"));

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testAddNewType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData(); // load engines
        EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);

        Engine e1 = engineManager.getByRoadAndNumber("PC", "5524");
        e1.setTypeName("TEST_TYPE");

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();

        // should cause add type dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(e1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addType"), Bundle.getMessage("ButtonNo"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertFalse(InstanceManager.getDefault(EngineTypes.class).containsName("TEST_TYPE"));

        // now answer yes to add type
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(e1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addType"), Bundle.getMessage("ButtonYes"));

        try {
            load2.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertTrue(InstanceManager.getDefault(EngineTypes.class).containsName("TEST_TYPE"));

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testAddNewLength() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData(); // load engines
        EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);

        Engine e1 = engineManager.getByRoadAndNumber("PC", "5524");
        e1.setLength("1234");

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();

        // should cause add length dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(e1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addLength"), Bundle.getMessage("ButtonNo"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertFalse(InstanceManager.getDefault(EngineLengths.class).containsName("1234"));

        // now answer yes to add type
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(e1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addLength"), Bundle.getMessage("ButtonYes"));

        try {
            load2.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertTrue(InstanceManager.getDefault(EngineLengths.class).containsName("1234"));

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testAddOwner() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData(); // load engines
        EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);

        Engine e1 = engineManager.getByRoadAndNumber("PC", "5524");
        e1.setOwner("TEST_OWNER");

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();

        // should cause add owner dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(e1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addOwner"), Bundle.getMessage("ButtonNo"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertFalse(InstanceManager.getDefault(CarOwners.class).containsName("TEST_OWNER"));

        // now answer yes to add owner
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(e1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addOwner"), Bundle.getMessage("ButtonYes"));

        try {
            load2.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertTrue(InstanceManager.getDefault(CarOwners.class).containsName("TEST_OWNER"));

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testSaveConsist() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData(); // load engines
        EngineManager engineManager = InstanceManager.getDefault(EngineManager.class);

        Engine e1 = engineManager.getByRoadAndNumber("PC", "5524");
        Assert.assertEquals("consist name", "C14", e1.getConsistName());

        EngineEditFrame f = new EngineEditFrame();
        f.initComponents();
        f.load(e1);

        Consist c = engineManager.newConsist("TEST_CONSIST");
        f.groupComboBox.setSelectedItem(c.getName());
        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertEquals("consist name", "TEST_CONSIST", e1.getConsistName());

        JUnitUtil.dispose(f);
    }
}
