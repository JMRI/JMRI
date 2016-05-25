package jmri.jmrit.operations.rollingstock.cars;

import javax.swing.JComboBox;
import jmri.jmrit.operations.OperationsTestCase;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations RollingStock Cars class Last manually cross-checked
 * on 20090131
 *
 * Still to do: Everything
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class CarLengthsTest extends OperationsTestCase {

    public void testCarLengths() {
        CarLengths cl1 = CarLengths.instance();
        cl1.getNames();	// load predefined lengths

        Assert.assertTrue("Car Length Predefined 40", cl1.containsName("40"));
        Assert.assertTrue("Car Length Predefined 32", cl1.containsName("32"));
        Assert.assertTrue("Car Length Predefined 60", cl1.containsName("60"));

        cl1.addName("1");
        Assert.assertTrue("Car Length Add 1", cl1.containsName("1"));
        Assert.assertFalse("Car Length Never Added 13", cl1.containsName("13"));
        cl1.addName("2");
        Assert.assertTrue("Car Length Still Has 1", cl1.containsName("1"));
        Assert.assertTrue("Car Length Add s2", cl1.containsName("2"));
        String[] lengths = cl1.getNames();
        Assert.assertEquals("First length name", "2", lengths[0]);
        Assert.assertEquals("2nd length name", "1", lengths[1]);
        JComboBox<?> box = cl1.getComboBox();
        Assert.assertEquals("First comboBox length name", "2", box.getItemAt(0));
        Assert.assertEquals("2nd comboBox length name", "1", box.getItemAt(1));
        cl1.deleteName("2");
        Assert.assertFalse("Car Length Delete 2", cl1.containsName("2"));
        cl1.deleteName("1");
        Assert.assertFalse("Car Length Delete 1", cl1.containsName("1"));
    }

    public void testCarOwners() {
        CarOwners co1 = CarOwners.instance();

        co1.addName("Rich Guy 1");
        Assert.assertTrue("Car Owner Add", co1.containsName("Rich Guy 1"));
        Assert.assertFalse("Car Owner Never Added", co1.containsName("Richer Guy 2"));
        co1.addName("Really Rich 3");
        Assert.assertTrue("Car Owner Still Has", co1.containsName("Rich Guy 1"));
        Assert.assertTrue("Car Owner Add second", co1.containsName("Really Rich 3"));
        String[] owners = co1.getNames();
        Assert.assertEquals("First owner name", "Really Rich 3", owners[0]);
        Assert.assertEquals("2nd owner name", "Rich Guy 1", owners[1]);
        JComboBox<?> box = co1.getComboBox();
        Assert.assertEquals("First comboBox owner name", "Really Rich 3", box.getItemAt(0));
        Assert.assertEquals("2nd comboBox owner name", "Rich Guy 1", box.getItemAt(1));
        co1.deleteName("Really Rich 3");
        Assert.assertFalse("Car Owner Delete", co1.containsName("Really Rich 3"));
        co1.deleteName("Rich Guy 1");
        Assert.assertFalse("Car Owner Delete second", co1.containsName("Rich Guy 1"));
    }

    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public CarLengthsTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CarLengthsTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CarLengthsTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
       super.tearDown();
    }
}
