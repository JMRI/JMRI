package jmri.jmrit.operations.rollingstock.cars;

import javax.swing.JComboBox;
import jmri.jmrit.operations.OperationsTestCase;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations CarTypes class Last manually cross-checked
 * on 20090131
 *
 * Derived from prevous "OperationsCarTest" to include only the tests related to * CarTypes.
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class CarTypesTest extends OperationsTestCase {

    public void testCarTypes() {
        CarTypes ct1 = CarTypes.instance();
        ct1.getNames();	//Load predefined car types

        Assert.assertTrue("Car Types Predefined Boxcar", ct1.containsName("Boxcar"));
        Assert.assertTrue("Car Types Predefined Caboose", ct1.containsName("Caboose"));

        ct1.addName("Type New1");
        Assert.assertTrue("Car Types Add New1", ct1.containsName("Type New1"));
        Assert.assertFalse("Car Types Never Added New2", ct1.containsName("Type New2"));
        ct1.addName("Type New3");
        Assert.assertTrue("Car Types Still Has New1", ct1.containsName("Type New1"));
        Assert.assertTrue("Car Types Add New3", ct1.containsName("Type New3"));
        ct1.replaceName("Type New3", "Type New4");
        Assert.assertFalse("Car Types replace New3", ct1.containsName("Type New3"));
        Assert.assertTrue("Car Types replace New3 with New4", ct1.containsName("Type New4"));
        String[] types = ct1.getNames();
        Assert.assertEquals("First type name", "Type New4", types[0]);
        Assert.assertEquals("2nd type name", "Type New1", types[1]);
        JComboBox<?> box = ct1.getComboBox();
        Assert.assertEquals("First comboBox type name", "Type New4", box.getItemAt(0));
        Assert.assertEquals("2nd comboBox type name", "Type New1", box.getItemAt(1));
        ct1.deleteName("Type New4");
        Assert.assertFalse("Car Types Delete New4", ct1.containsName("Type New4"));
        ct1.deleteName("Type New1");
        Assert.assertFalse("Car Types Delete New1", ct1.containsName("Type New1"));
    }
    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public CarTypesTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CarTypesTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CarTypesTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
       super.tearDown();
    }
}
