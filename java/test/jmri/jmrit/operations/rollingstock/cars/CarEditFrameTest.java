//CarEditFrameTest.java
package jmri.jmrit.operations.rollingstock.cars;

import java.util.List;
import jmri.jmrit.operations.OperationsSwingTestCase;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations Cars GUI class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 */
public class CarEditFrameTest extends OperationsSwingTestCase {

    List<String> tempCars;

    public void testCarEditFrame() {
        loadCars();		// load cars
        CarManager cManager = CarManager.instance();
        Assert.assertEquals("number of cars", 5, cManager.getNumEntries());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        f.setTitle("Test Add Car Frame");

        // add a new car
        f.roadNumberTextField.setText("6");
        f.roadComboBox.setSelectedItem("SP");
        f.typeComboBox.setSelectedItem("Caboose");
        f.lengthComboBox.setSelectedItem("38");
        f.colorComboBox.setSelectedItem("Black");
        f.loadComboBox.setSelectedItem("L");
        f.builtTextField.setText("1999");
        f.ownerComboBox.setSelectedItem("Owner1");
        f.commentTextField.setText("test car comment field");
        
        // Save button should be disabled
        getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));

        Car c6 = cManager.getByRoadAndNumber("SP", "6");
        Assert.assertNull("Car should not exist", c6);
        
        // use add button
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));

        c6 = cManager.getByRoadAndNumber("SP", "6");
        Assert.assertNotNull("Car did not create", c6);
        Assert.assertEquals("car type", "Caboose", c6.getTypeName());
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

        getHelper().enterClickAndLeave(new MouseEventData(this, f.cabooseCheckBox));
        Assert.assertFalse("still not a caboose", c6.isCaboose());
        getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));
        // Change all car type to caboose dialog window should appear
        // need to push the "No" button in the dialog window to close
        pressDialogButton(f, "No");

        Assert.assertTrue("now a caboose", c6.isCaboose());
        Assert.assertFalse("not hazardous 2", c6.isHazardous());

        getHelper().enterClickAndLeave(new MouseEventData(this, f.fredCheckBox));
        Assert.assertTrue("still a caboose", c6.isCaboose());
        Assert.assertFalse("still no fred", c6.hasFred());
        getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));
        // need to push the "No" button in the dialog window to close
        pressDialogButton(f, "No");
        Assert.assertFalse("no longer a caboose", c6.isCaboose());
        Assert.assertTrue("now has a fred", c6.hasFred());
        Assert.assertFalse("not hazardous 3", c6.isHazardous());

        getHelper().enterClickAndLeave(new MouseEventData(this, f.hazardousCheckBox));
        Assert.assertFalse("still not hazardous 3", c6.isHazardous());
        getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));
        // need to push the "No" button in the dialog window to close
        pressDialogButton(f, "No");
        Assert.assertFalse("still no longer a caboose", c6.isCaboose());
        Assert.assertTrue("still has a fred", c6.hasFred());
        Assert.assertTrue("now hazardous", c6.isHazardous());

        getHelper().enterClickAndLeave(new MouseEventData(this, f.utilityCheckBox));
        Assert.assertFalse("not utility", c6.isUtility());
        getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));
        // need to push the "No" button in the dialog window to close
        pressDialogButton(f, "No");
        Assert.assertTrue("now utility", c6.isUtility());
        Assert.assertFalse("not a caboose", c6.isCaboose());
        Assert.assertTrue("still has a fred", c6.hasFred());
        Assert.assertTrue("still hazardous", c6.isHazardous());

        // should have 6 cars now
        Assert.assertEquals("number of cars", 6, cManager.getNumEntries());

        f.dispose();
    }

    public void testCarEditFrameRead() {
        loadCars();		// load cars
        CarManager cManager = CarManager.instance();
        // should have 5 cars now
        Assert.assertEquals("number of cars", 5, cManager.getNumEntries());
        Car c1 = cManager.getByRoadAndNumber("NH", "1");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        f.loadCar(c1);
        f.setTitle("Test Edit Car Frame");

        Assert.assertEquals("car road", "NH", f.roadComboBox.getSelectedItem());
        Assert.assertEquals("car number", "1", f.roadNumberTextField.getText());
        Assert.assertEquals("car type", "Caboose", f.typeComboBox.getSelectedItem());
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
        getHelper().enterClickAndLeave(new MouseEventData(this, f.deleteButton));

        // should have 5 cars now
        Assert.assertEquals("number of cars", 4, cManager.getNumEntries());

        f.dispose();
    }

    private void loadCars() {
        CarManager cManager = CarManager.instance();
        // remove previous cars
        cManager.dispose();
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
        c1.setTypeName("Caboose");
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
    protected void setUp() throws Exception {
        super.setUp();
    }

    public CarEditFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CarEditFrameTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CarEditFrameTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
