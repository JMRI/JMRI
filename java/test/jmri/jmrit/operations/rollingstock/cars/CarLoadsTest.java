package jmri.jmrit.operations.rollingstock.cars;

import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Operations RollingStock Cars Loads class Last manually
 * cross-checked on 20090131
 * <p>
 * Still to do: Everything
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class CarLoadsTest extends OperationsTestCase {

    @Test
    public void testDefaultCarLoads() {
        CarLoads cl = InstanceManager.getDefault(CarLoads.class);

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
    }

    @Test
    public void testAddCarLoads() {
        CarLoads cl = InstanceManager.getDefault(CarLoads.class);

        cl.addName("BoXcaR", "New Boxcar Load");
        cl.addName("bOxCaR", "A boxcar load");
        cl.addName("bOxCaR", "B boxcar load");
        List<String> names = cl.getNames("BoXcaR");

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

    }

    @Test
    public void testReplaceCarLoads() {
        CarLoads cl = InstanceManager.getDefault(CarLoads.class);
        cl.addName("bOxCaR", "A boxcar load");
        cl.addName("bOxCaR", "B boxcar load");
        cl.replaceName("bOxCaR", "A boxcar load", "C boxcar load");

        List<String> names = cl.getNames("bOxCaR");

        Assert.assertEquals("number of names", 4, names.size());
        Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
        Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));
        Assert.assertFalse("new load", cl.containsName("bOxCaR", "A boxcar load"));
        Assert.assertTrue("new load", cl.containsName("bOxCaR", "B boxcar load"));
        Assert.assertTrue("new load", cl.containsName("bOxCaR", "C boxcar load"));

    }

    @Test
    public void testDeleteCarLoads() {
        CarLoads cl = InstanceManager.getDefault(CarLoads.class);
        cl.addName("BoXcaR", "New Boxcar Load");
        cl.addName("bOxCaR", "A boxcar load");
        cl.addName("bOxCaR", "B boxcar load");
        cl.replaceName("bOxCaR", "A boxcar load", "C boxcar load");
        cl.deleteName("bOxCaR", "B boxcar load");

        List<String> names = cl.getNames("bOxCaR");

        Assert.assertEquals("number of names", 3, names.size());
        Assert.assertTrue("Default load", cl.containsName("bOxCaR", "L"));
        Assert.assertTrue("Default empty", cl.containsName("bOxCaR", "E"));
        Assert.assertFalse("new load", cl.containsName("bOxCaR", "A boxcar load"));
        Assert.assertFalse("new load", cl.containsName("bOxCaR", "B boxcar load"));
        Assert.assertTrue("new load", cl.containsName("bOxCaR", "C boxcar load"));
    }

    @Test
    public void testGetAndSetDefaultEmptyCarLoads() {
        CarLoads cl = InstanceManager.getDefault(CarLoads.class);
        Assert.assertEquals("default empty", "E", cl.getDefaultEmptyName());
        Assert.assertEquals("default load", "L", cl.getDefaultLoadName());

        cl.addName("bOxCaR", "C boxcar load");

        cl.setDefaultEmptyName("E<mpty>");
        cl.setDefaultLoadName("L<oad>");

        Assert.assertEquals("default empty", "E<mpty>", cl.getDefaultEmptyName());
        Assert.assertEquals("default load", "L<oad>", cl.getDefaultLoadName());

        List <String> names = cl.getNames("BOXCAR");

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
}
