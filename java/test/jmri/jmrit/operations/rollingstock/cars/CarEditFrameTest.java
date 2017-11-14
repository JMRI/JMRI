//CarEditFrameTest.java
package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GraphicsEnvironment;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations Cars GUI class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 */
public class CarEditFrameTest extends OperationsSwingTestCase {

    List<String> tempCars;

    @Test
    public void testCarEditFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        loadCars();		// load cars
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
        //enterClickAndLeave(f.saveButton);
        Car c6 = cManager.getByRoadAndNumber("SP", "6");
        Assert.assertNull("Car should not exist", c6);

        // use add button
        enterClickAndLeave(f.addButton);

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

        enterClickAndLeave(f.cabooseCheckBox);
        Assert.assertFalse("still not a caboose", c6.isCaboose());
        enterClickAndLeave(f.saveButton);
        // Change all car type to caboose dialog window should appear
        // need to push the "No" button in the dialog window to close
        pressDialogButton(f, Bundle.getMessage("carModifyAllType", new Object[]{Bundle.getMessage("Caboose")}), Bundle.getMessage("ButtonNo"));

        Assert.assertTrue("now a caboose", c6.isCaboose());
        Assert.assertFalse("not hazardous 2", c6.isHazardous());

        enterClickAndLeave(f.fredCheckBox);
        Assert.assertTrue("still a caboose", c6.isCaboose());
        Assert.assertFalse("still no fred", c6.hasFred());
        enterClickAndLeave(f.saveButton);
        // need to push the "No" button in the dialog window to close
        pressDialogButton(f, Bundle.getMessage("carModifyAllType", new Object[]{Bundle.getMessage("Caboose")}), Bundle.getMessage("ButtonNo"));
        Assert.assertFalse("no longer a caboose", c6.isCaboose());
        Assert.assertTrue("now has a fred", c6.hasFred());
        Assert.assertFalse("not hazardous 3", c6.isHazardous());

        enterClickAndLeave(f.hazardousCheckBox);
        Assert.assertFalse("still not hazardous 3", c6.isHazardous());
        enterClickAndLeave(f.saveButton);
        // need to push the "No" button in the dialog window to close
        pressDialogButton(f, Bundle.getMessage("carModifyAllType", new Object[]{Bundle.getMessage("Caboose")}), Bundle.getMessage("ButtonNo"));
        Assert.assertFalse("still no longer a caboose", c6.isCaboose());
        Assert.assertTrue("still has a fred", c6.hasFred());
        Assert.assertTrue("now hazardous", c6.isHazardous());

        enterClickAndLeave(f.utilityCheckBox);
        Assert.assertFalse("not utility", c6.isUtility());
        enterClickAndLeave(f.saveButton);
        // need to push the "No" button in the dialog window to close
        pressDialogButton(f, Bundle.getMessage("carModifyAllType", new Object[]{Bundle.getMessage("Caboose")}), Bundle.getMessage("ButtonNo"));
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
        loadCars();		// load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        // should have 5 cars now
        Assert.assertEquals("number of cars", 5, cManager.getNumEntries());
        Car c1 = cManager.getByRoadAndNumber("NH", "1");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        f.loadCar(c1);
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
        enterClickAndLeave(f.deleteButton);

        // should have 5 cars now
        Assert.assertEquals("number of cars", 4, cManager.getNumEntries());

        JUnitUtil.dispose(f);
    }

    private void loadCars() {
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        // remove previous cars
        cManager.dispose();
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
        Car c1 = cManager.newCar("NH", "1");
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

        Car c2 = cManager.newCar("UP", "22");
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

        Car c3 = cManager.newCar("AA", "3");
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

        Car c4 = cManager.newCar("SP", "2");
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

        Car c5 = cManager.newCar("NH", "5");
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
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
