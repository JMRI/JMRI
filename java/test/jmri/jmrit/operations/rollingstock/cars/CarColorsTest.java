// CarColorsTest.java
package jmri.jmrit.operations.rollingstock.cars;

import javax.swing.JComboBox;
import jmri.jmrit.operations.OperationsTestCase;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations RollingStock CarColors class Last manually cross-checked
 * on 20090131
 *
 * Still to do: Everything
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class CarColorsTest extends OperationsTestCase {

    public void testCarColors() {
        CarColors cc1 = CarColors.instance();
        cc1.getNames();	// load predefined colors

        Assert.assertTrue("Car Color Predefined Red", cc1.containsName("Red"));
        Assert.assertTrue("Car Color Predefined Blue", cc1.containsName("Blue"));

        cc1.addName("BoxCar Red");
        Assert.assertTrue("Car Color Add", cc1.containsName("BoxCar Red"));
        Assert.assertFalse("Car Color Never Added Dirty Blue", cc1.containsName("Dirty Blue"));
        cc1.addName("Ugly Brown");
        Assert.assertTrue("Car Color Still Has BoxCar Red", cc1.containsName("BoxCar Red"));
        Assert.assertTrue("Car Color Add Ugly Brown", cc1.containsName("Ugly Brown"));
        String[] colors = cc1.getNames();
        Assert.assertEquals("First color name", "Ugly Brown", colors[0]);
        Assert.assertEquals("2nd color name", "BoxCar Red", colors[1]);
        JComboBox<?> box = cc1.getComboBox();
        Assert.assertEquals("First comboBox color name", "Ugly Brown", box.getItemAt(0));
        Assert.assertEquals("2nd comboBox color name", "BoxCar Red", box.getItemAt(1));
        cc1.deleteName("Ugly Brown");
        Assert.assertFalse("Car Color Delete Ugly Brown", cc1.containsName("Ugly Brown"));
        cc1.deleteName("BoxCar Red");
        Assert.assertFalse("Car Color Delete BoxCar Red", cc1.containsName("BoxCar Red"));
    }

    @Override
    protected void setUp() throws Exception{
        super.setUp();
    }

    public CarColorsTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CarColorsTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CarColorsTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
       super.tearDown();
    }
}
