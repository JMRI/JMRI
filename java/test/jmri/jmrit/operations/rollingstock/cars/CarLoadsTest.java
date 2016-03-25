// CarLoadsTest.java
package jmri.jmrit.operations.rollingstock.cars;

import java.util.List;
import jmri.jmrit.operations.OperationsTestCase;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations RollingStock Cars Loads class Last manually cross-checked
 * on 20090131
 *
 * Still to do: Everything
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class CarLoadsTest extends OperationsTestCase {

    public void testCarLoads() {
        CarLoads cl = CarLoads.instance();
        
        // confirm proper defaults
        Assert.assertEquals("Default car empty", "E", cl.getDefaultEmptyName());
        Assert.assertEquals("Default car load", "L", cl.getDefaultLoadName());
        
        List<String> names = cl.getNames("BoXcaR");

        Assert.assertEquals("Two default names", 2, names.size());
        Assert.assertTrue("Default load", cl.containsName("BoXcaR", "L"));
        Assert.assertTrue("Default empty", cl.containsName("BoXcaR", "E"));

        names = cl.getNames("bOxCaR");

        Assert.assertEquals("Two default names", 2, names.size());
        Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
        Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));

        cl.addName("BoXcaR", "New Boxcar Load");
        cl.addName("bOxCaR", "A boxcar load");
        cl.addName("bOxCaR", "B boxcar load");
        names = cl.getNames("BoXcaR");

        Assert.assertEquals("number of names", 3, names.size());
        Assert.assertTrue("Default load", cl.containsName("BoXcaR", "L"));
        Assert.assertTrue("Default empty", cl.containsName("BoXcaR", "E"));
        Assert.assertTrue("new load", cl.containsName("BoXcaR", "New Boxcar Load"));

        names = cl.getNames("bOxCaR");

        Assert.assertEquals("number of names", 4, names.size());
        Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
        Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));
        Assert.assertTrue("new load", cl.containsName("bOxCaR", "A boxcar load"));
        Assert.assertTrue("new load", cl.containsName("bOxCaR", "B boxcar load"));

        cl.replaceName("bOxCaR", "A boxcar load", "C boxcar load");

        names = cl.getNames("bOxCaR");

        Assert.assertEquals("number of names", 4, names.size());
        Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
        Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));
        Assert.assertFalse("new load", cl.containsName("bOxCaR", "A boxcar load"));
        Assert.assertTrue("new load", cl.containsName("bOxCaR", "B boxcar load"));
        Assert.assertTrue("new load", cl.containsName("bOxCaR", "C boxcar load"));

        cl.deleteName("bOxCaR", "B boxcar load");

        names = cl.getNames("bOxCaR");

        Assert.assertEquals("number of names", 3, names.size());
        Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
        Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));
        Assert.assertFalse("new load", cl.containsName("bOxCaR", "A boxcar load"));
        Assert.assertFalse("new load", cl.containsName("bOxCaR", "B boxcar load"));
        Assert.assertTrue("new load", cl.containsName("bOxCaR", "C boxcar load"));

        Assert.assertEquals("default empty", "E", cl.getDefaultEmptyName());
        Assert.assertEquals("default load", "L", cl.getDefaultLoadName());

        cl.setDefaultEmptyName("E<mpty>");
        cl.setDefaultLoadName("L<oad>");

        Assert.assertEquals("default empty", "E<mpty>", cl.getDefaultEmptyName());
        Assert.assertEquals("default load", "L<oad>", cl.getDefaultLoadName());

        names = cl.getNames("BOXCAR");

        Assert.assertEquals("number of names", 2, names.size());
        Assert.assertFalse("Default load", cl.containsName("BOXCAR", "L"));
        Assert.assertFalse("Default empty", cl.containsName("BOXCAR", "E"));
        Assert.assertTrue("Default load", cl.containsName("BOXCAR", "L<oad>"));
        Assert.assertTrue("Default empty", cl.containsName("BOXCAR", "E<mpty>"));

        // bOxCaR was created using old defaults
        Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
        Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));
        Assert.assertTrue("new load", cl.containsName("bOxCaR", "C boxcar load"));

        cl.setDefaultEmptyName("E");
        cl.setDefaultLoadName("L");

        Assert.assertEquals("default empty", "E", cl.getDefaultEmptyName());
        Assert.assertEquals("default load", "L", cl.getDefaultLoadName());
    }

    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public CarLoadsTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CarLoadsTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CarLoadsTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
       super.tearDown();
    }
}
