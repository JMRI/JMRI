package jmri.jmrit.operations.rollingstock.cars;

import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the Operations RollingStock Cars class Last manually cross-checked
 * on 20090131
 * <p>
 * Still to do: Everything
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class CarLengthsTest extends OperationsTestCase {

    public void testDefaultCarLengths() {
        CarLengths cl1 = InstanceManager.getDefault(CarLengths.class);

        // some Locales define different car length sets.
        Assert.assertNotNull("Car Length defined", cl1.getNames());
    }

    public void testAddAndDeleteCarLengths() {
        CarLengths cl1 = InstanceManager.getDefault(CarLengths.class);
        cl1.getNames();	// load predefined lengths

        cl1.addName("1");
        cl1.deleteName("13"); // en_GB defines a length 13.
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
        junit.textui.TestRunner.main(testCaseName);
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
