package jmri.jmrit.operations.rollingstock.cars;

import javax.swing.JComboBox;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;

/**
 * Tests for the Operations RollingStock Cars Roads class Last manually
 * cross-checked on 20090131
 * <p>
 * Still to do: Everything
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
 */
public class CarRoadsTest extends OperationsTestCase {

    @Test
    public void testDefaultCarRoads() {
        CarRoads cr1 = InstanceManager.getDefault(CarRoads.class);

        // the previous version of this test looked for specific values,
        // but those specific road names may not exist in a non-US 
        // English context. 
        Assert.assertNotNull("Car Roads not empty", cr1.getNames());
    }

    @Test
    public void testAddAndDeleteCarRoads() {
        CarRoads cr1 = InstanceManager.getDefault(CarRoads.class);
        String[] roads = cr1.getNames(); // load predefined roads
        Assert.assertEquals("default number of roads", 133, roads.length);
        cr1.addName("BB New1");
        Assert.assertTrue("Car Roads Add New1", cr1.containsName("BB New1"));
        Assert.assertFalse("Car Roads Never Added New2", cr1.containsName("BB New2"));
        cr1.addName("BB New3");
        Assert.assertTrue("Car Roads Still Has New1", cr1.containsName("BB New1"));
        Assert.assertTrue("Car Roads Add New3", cr1.containsName("BB New3"));
        cr1.replaceName("BB New3", "BB New4");
        Assert.assertFalse("Car Roads replace New3", cr1.containsName("BB New3"));
        Assert.assertTrue("Car Roads replace New3 with New4", cr1.containsName("BB New4"));
        roads = cr1.getNames();
        Assert.assertEquals("First road name", "BB New4", roads[13]);
        Assert.assertEquals("2nd road name", "BB New1", roads[12]);
        JComboBox<?> box = cr1.getComboBox();
        Assert.assertEquals("First comboBox road name", "BB New4", box.getItemAt(13));
        Assert.assertEquals("2nd comboBox road name", "BB New1", box.getItemAt(12));
        cr1.deleteName("BB New4");
        Assert.assertFalse("Car Roads Delete New4", cr1.containsName("BB New4"));
        cr1.deleteName("BB New1");
        Assert.assertFalse("Car Roads Delete New1", cr1.containsName("BB New1"));
    }
}
