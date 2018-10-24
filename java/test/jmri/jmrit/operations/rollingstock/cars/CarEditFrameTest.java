package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations Cars GUI class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class CarEditFrameTest extends OperationsTestCase {

    @Test
    public void testCarEditFrameErrorConditions() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        Assert.assertTrue(f.isShowing());

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

        // clear road number
        JemmyUtil.enterClickAndLeave(f.clearRoadNumberButton);

        // new dialog warning car weight
        JemmyUtil.enterClickAndLeave(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("carActualWeight"), Bundle.getMessage("ButtonOK"));

        f.weightTextField.setText("1.5"); // good oz weight      
        f.weightTonsTextField.setText("Bogus Weight");
        // new dialog warning car weight
        JemmyUtil.enterClickAndLeave(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("carWeightTon"), Bundle.getMessage("ButtonOK"));

        // this will load the weight fields
        f.lengthComboBox.setSelectedIndex(4);

        // confirm that delete and save buttons are NOT enabled
        Assert.assertFalse(f.saveButton.isEnabled());
        Assert.assertFalse(f.deleteButton.isEnabled());

        JemmyUtil.enterClickAndLeave(f.addButton);

        // confirm that delete and save buttons are enabled
        Assert.assertTrue(f.saveButton.isEnabled());
        Assert.assertTrue(f.deleteButton.isEnabled());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarEditFrameLocationComboBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        
        LocationManager lm = InstanceManager.getDefault(LocationManager.class);
        Location loc1 = lm.getLocationByName("North End Staging");
        Location loc2 = lm.getLocationByName("North Industries");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        Assert.assertTrue(f.isShowing());

        f.roadNumberTextField.setText("10345");
        f.roadComboBox.setSelectedItem("SP");
        
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Car car = cm.getByRoadAndNumber("SP", "10345");
        Assert.assertNull("car exists", car);

        // this will load the weight fields
        f.lengthComboBox.setSelectedIndex(4); //40 foot car

        // test no track selected error
        f.locationBox.setSelectedIndex(1);
        JemmyUtil.enterClickAndLeave(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"));
        
        car = cm.getByRoadAndNumber("SP", "10345");
        Assert.assertNotNull("car exists", car);
        
        Assert.assertEquals("car location", null, car.getLocation());

        f.trackLocationBox.setSelectedIndex(1);
        JemmyUtil.enterClickAndLeave(f.addButton);
        
        // car already exists
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("carCanNotAdd"), Bundle.getMessage("ButtonOK"));
        Assert.assertEquals("car location", null, car.getLocation());
        
        JemmyUtil.enterClickAndLeave(f.saveButton);        
        // car location should have been updated
        Assert.assertEquals("car location", loc1, car.getLocation());
        
        // now change location
        f.locationBox.setSelectedIndex(2);
        f.trackLocationBox.setSelectedIndex(1);

        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertEquals("car location", loc2, car.getLocation());
             
        // add a track
        Assert.assertEquals("Number of locations", 4, f.locationBox.getItemCount());
        Assert.assertEquals("Number of tracks", 2, f.trackLocationBox.getItemCount());
        Track testSpur = loc2.addTrack("Test_Spur", Track.SPUR);
        
        // add a location to cause update to both location and track comboboxes
        lm.newLocation("Test_Location");
        Assert.assertEquals("Number of locations", 5, f.locationBox.getItemCount());
        Assert.assertEquals("Number of tracks", 3, f.trackLocationBox.getItemCount());
        
        // try to set car to test spur, with a length of 0
        f.trackLocationBox.setSelectedIndex(2);
        JemmyUtil.enterClickAndLeave(f.saveButton);
        
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"));
        
        // get response message
        String status = car.setLocation(loc2, testSpur);
        Assert.assertFalse(status.equals(Track.OKAY));
        JemmyUtil.pressDialogButton(f, MessageFormat
                .format(Bundle.getMessage("rsOverride"), new Object[]{status}), Bundle.getMessage("ButtonNo"));
   
        // confirm car location and track didn't change
        Assert.assertNotEquals("track", testSpur, car.getTrack());
        
        // do it again, but say yes
       JemmyUtil.enterClickAndLeave(f.saveButton);
        
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.pressDialogButton(f, MessageFormat
                .format(Bundle.getMessage("rsOverride"), new Object[]{status}), Bundle.getMessage("ButtonYes"));
   
        // confirm car location and track changed
        Assert.assertEquals("track", testSpur, car.getTrack());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarEditFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        loadCars(); // load cars

        // increase test coverage
        Setup.setValueEnabled(false);
        Setup.setRfidEnabled(false);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("number of cars", 5, cManager.getNumEntries());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        f.setTitle("Test Add Car Frame");

        // add a new car
        f.roadNumberTextField.setText("6");
        f.roadComboBox.setSelectedItem("SP");
        f.typeComboBox.setSelectedItem(Bundle.getMessage("Caboose"));
        f.lengthComboBox.setSelectedItem("38");
        f.colorComboBox.setSelectedItem("Black");
        f.loadComboBox.setSelectedItem("L");
        f.builtTextField.setText("1999");
        f.ownerComboBox.setSelectedItem("Owner1");
        f.commentTextField.setText("test car comment field");

        // Save button should be disabled
        // Jemmy has no way to click a disabled button.
        //JemmyUtil.enterClickAndLeave(f.saveButton);
        Car c6 = cManager.getByRoadAndNumber("SP", "6");
        Assert.assertNull("Car should not exist", c6);

        // use add button
        JemmyUtil.enterClickAndLeave(f.addButton);

        c6 = cManager.getByRoadAndNumber("SP", "6");
        Assert.assertNotNull("Car did not create", c6);
        Assert.assertEquals("car type", Bundle.getMessage("Caboose"), c6.getTypeName());
        Assert.assertEquals("car length", "38", c6.getLength());
        Assert.assertEquals("car color", "Black", c6.getColor());
        Assert.assertEquals("car load", "L", c6.getLoadName());
        Assert.assertEquals("car built", "1999", c6.getBuilt());
        Assert.assertEquals("car owner", "Owner1", c6.getOwner());
        Assert.assertEquals("car comment", "test car comment field", c6.getComment());

        // test type default check boxes
        Assert.assertFalse("not a caboose", c6.isCaboose());
        Assert.assertFalse("no fred", c6.hasFred());
        Assert.assertFalse("not hazardous", c6.isHazardous());

        JemmyUtil.enterClickAndLeave(f.cabooseCheckBox);
        Assert.assertFalse("still not a caboose", c6.isCaboose());
        JemmyUtil.enterClickAndLeave(f.saveButton);
        // Change all car type to caboose dialog window should appear
        // need to push the "No" button in the dialog window to close
        JemmyUtil.pressDialogButton(f,
                Bundle.getMessage("carModifyAllType", new Object[]{Bundle.getMessage("Caboose")}),
                Bundle.getMessage("ButtonNo"));

        Assert.assertTrue("now a caboose", c6.isCaboose());
        Assert.assertFalse("not hazardous 2", c6.isHazardous());

        JemmyUtil.enterClickAndLeave(f.fredCheckBox);
        Assert.assertTrue("still a caboose", c6.isCaboose());
        Assert.assertFalse("still no fred", c6.hasFred());
        JemmyUtil.enterClickAndLeave(f.saveButton);
        // need to push the "No" button in the dialog window to close
        JemmyUtil.pressDialogButton(f,
                Bundle.getMessage("carModifyAllType", new Object[]{Bundle.getMessage("Caboose")}),
                Bundle.getMessage("ButtonNo"));
        Assert.assertFalse("no longer a caboose", c6.isCaboose());
        Assert.assertTrue("now has a fred", c6.hasFred());
        Assert.assertFalse("not hazardous 3", c6.isHazardous());

        JemmyUtil.enterClickAndLeave(f.hazardousCheckBox);
        Assert.assertFalse("still not hazardous 3", c6.isHazardous());
        JemmyUtil.enterClickAndLeave(f.saveButton);
        // need to push the "No" button in the dialog window to close
        JemmyUtil.pressDialogButton(f,
                Bundle.getMessage("carModifyAllType", new Object[]{Bundle.getMessage("Caboose")}),
                Bundle.getMessage("ButtonNo"));
        Assert.assertFalse("still no longer a caboose", c6.isCaboose());
        Assert.assertTrue("still has a fred", c6.hasFred());
        Assert.assertTrue("now hazardous", c6.isHazardous());

        JemmyUtil.enterClickAndLeave(f.utilityCheckBox);
        Assert.assertFalse("not utility", c6.isUtility());
        JemmyUtil.enterClickAndLeave(f.saveButton);
        // need to push the "No" button in the dialog window to close
        JemmyUtil.pressDialogButton(f,
                Bundle.getMessage("carModifyAllType", new Object[]{Bundle.getMessage("Caboose")}),
                Bundle.getMessage("ButtonNo"));
        Assert.assertTrue("now utility", c6.isUtility());
        Assert.assertFalse("not a caboose", c6.isCaboose());
        Assert.assertTrue("still has a fred", c6.hasFred());
        Assert.assertTrue("still hazardous", c6.isHazardous());

        // should have 6 cars now
        Assert.assertEquals("number of cars", 6, cManager.getNumEntries());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarEditFrameRead() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        loadCars(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        // should have 5 cars now
        Assert.assertEquals("number of cars", 5, cManager.getNumEntries());
        Car c1 = cManager.getByRoadAndNumber("NH", "1");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        f.load(c1);
        f.setTitle("Test Edit Car Frame");

        Assert.assertEquals("car road", "NH", f.roadComboBox.getSelectedItem());
        Assert.assertEquals("car number", "1", f.roadNumberTextField.getText());
        Assert.assertEquals("car type", Bundle.getMessage("Caboose"), f.typeComboBox.getSelectedItem());
        Assert.assertEquals("car length", "40", f.lengthComboBox.getSelectedItem());
        Assert.assertEquals("car weight", "1.4", f.weightTextField.getText());
        Assert.assertEquals("car weight tons", "Tons of Weight", f.weightTonsTextField.getText());
        Assert.assertEquals("car color", "Red", f.colorComboBox.getSelectedItem());
        Assert.assertEquals("car load", "L", f.loadComboBox.getSelectedItem());
        Assert.assertEquals("car built", "2009", f.builtTextField.getText());
        Assert.assertEquals("car owner", "Owner2", f.ownerComboBox.getSelectedItem());
        Assert.assertEquals("car comment", "Test Car NH 1 Comment", f.commentTextField.getText());

        Assert.assertTrue("car is a caboose", f.cabooseCheckBox.isSelected());
        Assert.assertFalse("car does not have a fred", f.fredCheckBox.isSelected());
        Assert.assertFalse("car is not hazardous", f.hazardousCheckBox.isSelected());

        // test delete button
        JemmyUtil.enterClickAndLeave(f.deleteButton);

        // should have 5 cars now
        Assert.assertEquals("number of cars", 4, cManager.getNumEntries());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarEditFrameLoadNewCarRoad() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        loadCars(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("NH", "1");
        c1.setRoadName("TEST_ROAD");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        // should cause add road dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addRoad"), Bundle.getMessage("ButtonNo"));

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.TERMINATED);
        }, "wait to terminate");

        Assert.assertFalse(InstanceManager.getDefault(CarRoads.class).containsName("TEST_ROAD"));

        // now answer yes to add road
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addRoad"), Bundle.getMessage("ButtonYes"));

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.TERMINATED);
        }, "wait to terminate");

        Assert.assertTrue(InstanceManager.getDefault(CarRoads.class).containsName("TEST_ROAD"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarEditFrameLoadNewCarType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        loadCars(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("NH", "1");
        c1.setTypeName("TEST_TYPE");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        // should cause add type dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addType"), Bundle.getMessage("ButtonNo"));

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.TERMINATED);
        }, "wait to terminate");

        Assert.assertFalse(InstanceManager.getDefault(CarTypes.class).containsName("TEST_TYPE"));

        // now answer yes to add type
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addType"), Bundle.getMessage("ButtonYes"));

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.TERMINATED);
        }, "wait to terminate");

        Assert.assertTrue(InstanceManager.getDefault(CarTypes.class).containsName("TEST_TYPE"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarEditFrameLoadNewCarLength() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        loadCars(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("NH", "1");
        c1.setLength("123");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        // should cause add length dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addLength"), Bundle.getMessage("ButtonNo"));

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.TERMINATED);
        }, "wait to terminate");

        Assert.assertFalse(InstanceManager.getDefault(CarLengths.class).containsName("123"));

        // now answer yes to add length
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addLength"), Bundle.getMessage("ButtonYes"));

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.TERMINATED);
        }, "wait to terminate");

        Assert.assertTrue(InstanceManager.getDefault(CarLengths.class).containsName("123"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarEditFrameLoadNewCarOwner() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        loadCars(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("NH", "1");
        c1.setOwner("TEST_OWNER");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        // should cause add owner dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addOwner"), Bundle.getMessage("ButtonNo"));

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.TERMINATED);
        }, "wait to terminate");

        Assert.assertFalse(InstanceManager.getDefault(CarOwners.class).containsName("TEST_OWNER"));

        // now answer yes to add owner
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addOwner"), Bundle.getMessage("ButtonYes"));

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.TERMINATED);
        }, "wait to terminate");

        Assert.assertTrue(InstanceManager.getDefault(CarOwners.class).containsName("TEST_OWNER"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarEditFrameLoadNewCarColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        loadCars(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("NH", "1");
        c1.setColor("TEST_COLOR");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        // should cause add color dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("carAddColor"), Bundle.getMessage("ButtonNo"));

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.TERMINATED);
        }, "wait to terminate");

        Assert.assertFalse(InstanceManager.getDefault(CarColors.class).containsName("TEST_COLOR"));

        // now answer yes to add color
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("carAddColor"), Bundle.getMessage("ButtonYes"));

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.TERMINATED);
        }, "wait to terminate");

        Assert.assertTrue(InstanceManager.getDefault(CarColors.class).containsName("TEST_COLOR"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarEditFrameLoadNewCarLoad() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        loadCars(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("NH", "1");
        c1.setLoadName("TEST_LOAD");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        // should cause add load dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addLoad"), Bundle.getMessage("ButtonNo"));

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.TERMINATED);
        }, "wait to terminate");

        Assert.assertFalse(InstanceManager.getDefault(CarLoads.class).containsName(c1.getTypeName(), "TEST_LOAD"));

        // now answer yes to add load
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addLoad"), Bundle.getMessage("ButtonYes"));

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.TERMINATED);
        }, "wait to terminate");

        Assert.assertTrue(InstanceManager.getDefault(CarLoads.class).containsName(c1.getTypeName(), "TEST_LOAD"));

        JUnitUtil.dispose(f);
    }

    private void loadCars() {

        Setup.setValueEnabled(true);
        Setup.setRfidEnabled(true);
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        // add Owner1 and Owner2
        CarOwners co = InstanceManager.getDefault(CarOwners.class);
        co.addName("Owner1");
        co.addName("Owner2");
        // register the road names used
        CarRoads cr = InstanceManager.getDefault(CarRoads.class);
        cr.addName("UP");
        cr.addName("SP");
        cr.addName("NH");
        CarLengths cl = InstanceManager.getDefault(CarLengths.class);
        cl.addName("40");
        cl.addName("38");
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        ct.addName(Bundle.getMessage("Caboose"));
        // add 5 cars to table
        Car c1 = cManager.newRS("NH", "1");
        c1.setBuilt("2009");
        c1.setColor("Red");
        c1.setLength("40");
        c1.setLoadName("L");
        c1.setMoves(55);
        c1.setOwner("Owner2");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 3");
        c1.setRfid("RFID 3");
        c1.setTypeName(Bundle.getMessage("Caboose"));
        c1.setWeight("1.4");
        c1.setWeightTons("Tons of Weight");
        c1.setCaboose(true);
        c1.setComment("Test Car NH 1 Comment");

        Car c2 = cManager.newRS("UP", "22");
        c2.setBuilt("2004");
        c2.setColor("Blue");
        c2.setLength("50");
        c2.setLoadName("E");
        c2.setMoves(50);
        c2.setOwner("AT");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 2");
        c2.setRfid("RFID 2");
        c2.setTypeName("Boxcar");

        Car c3 = cManager.newRS("AA", "3");
        c3.setBuilt("2006");
        c3.setColor("White");
        c3.setLength("30");
        c3.setLoadName("LA");
        c3.setMoves(40);
        c3.setOwner("AB");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 5");
        c3.setRfid("RFID 5");
        c3.setTypeName("Gondola");

        Car c4 = cManager.newRS("SP", "2");
        c4.setBuilt("1990");
        c4.setColor("Black");
        c4.setLength("45");
        c4.setLoadName("EA");
        c4.setMoves(30);
        c4.setOwner("AAA");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 4");
        c4.setRfid("RFID 4");
        c4.setTypeName("Tank Food");

        Car c5 = cManager.newRS("NH", "5");
        c5.setBuilt("1956");
        c5.setColor("Brown");
        c5.setLength("25");
        c5.setLoadName("LL");
        c5.setMoves(25);
        c5.setOwner("DAB");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 1");
        c5.setRfid("RFID 1");
        c5.setTypeName("Coilcar");
    }

    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }
}
