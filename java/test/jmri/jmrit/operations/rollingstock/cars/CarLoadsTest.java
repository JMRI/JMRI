package jmri.jmrit.operations.rollingstock.cars;

import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;

/**
 * Tests for the Operations RollingStock Cars Loads class Last manually
 * cross-checked on 20090131
 * <p>
 * Still to do: Everything
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
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
    
    @Test
    public void testHazardousCarLoads() {
        CarLoads cl = InstanceManager.getDefault(CarLoads.class);
        Assert.assertFalse("Default not hazardous", cl.isHazardous("BoXcaR", "New Boxcar Load"));
        cl.addName("BoXcaR", "New Boxcar Load");
        cl.setHazardous("BoXcaR", "New Boxcar Load", true);
        Assert.assertTrue("Now hazardous", cl.isHazardous("BoXcaR", "New Boxcar Load"));
    }

    @Test
    public void testReplaceType() {
        CarLoads cl = InstanceManager.getDefault(CarLoads.class);
        cl.addName("BoXcaR", "LoadName");
        // don't use defaults to confirm proper operation
        cl.setLoadType("BoXcaR", "LoadName", CarLoad.LOAD_TYPE_EMPTY);
        cl.setPriority("BoXcaR", "LoadName", CarLoad.PRIORITY_HIGH);
        cl.setHazardous("BoXcaR", "LoadName", true);
        cl.setPickupComment("BoXcaR", "LoadName", "Pick up comment");
        cl.setDropComment("BoXcaR", "LoadName", "Drop comment");

        cl.replaceType("BoXcaR", "BOXCAR");

        Assert.assertEquals("Load type", CarLoad.LOAD_TYPE_EMPTY, cl.getLoadType("BOXCAR", "LoadName"));
        Assert.assertEquals("Load priority", CarLoad.PRIORITY_HIGH, cl.getPriority("BOXCAR", "LoadName"));
        Assert.assertEquals("Hazardous", true, cl.isHazardous("BOXCAR", "LoadName"));
        Assert.assertEquals("Pick up comment", "Pick up comment", cl.getPickupComment("BOXCAR", "LoadName"));
        Assert.assertEquals("Drop comment", "Drop comment", cl.getDropComment("BOXCAR", "LoadName"));
    }

}
