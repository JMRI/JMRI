package jmri.jmrit.operations.rollingstock.cars;

import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Operations RollingStock Cars Roads class Last manually
 * cross-checked on 20090131
 * <p>
 * Still to do: Everything
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
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
        cr1.getNames();	//load predefined roads
        cr1.addName("Road New1");
        Assert.assertTrue("Car Roads Add New1", cr1.containsName("Road New1"));
        Assert.assertFalse("Car Roads Never Added New2", cr1.containsName("Road New2"));
        cr1.addName("Road New3");
        Assert.assertTrue("Car Roads Still Has New1", cr1.containsName("Road New1"));
        Assert.assertTrue("Car Roads Add New3", cr1.containsName("Road New3"));
        cr1.replaceName("Road New3", "Road New4");
        Assert.assertFalse("Car Roads replace New3", cr1.containsName("Road New3"));
        Assert.assertTrue("Car Roads replace New3 with New4", cr1.containsName("Road New4"));
        String[] roads = cr1.getNames();
        Assert.assertEquals("First road name", "Road New4", roads[0]);
        Assert.assertEquals("2nd road name", "Road New1", roads[1]);
        JComboBox<?> box = cr1.getComboBox();
        Assert.assertEquals("First comboBox road name", "Road New4", box.getItemAt(0));
        Assert.assertEquals("2nd comboBox road name", "Road New1", box.getItemAt(1));
        cr1.deleteName("Road New4");
        Assert.assertFalse("Car Roads Delete New4", cr1.containsName("Road New4"));
        cr1.deleteName("Road New1");
        Assert.assertFalse("Car Roads Delete New1", cr1.containsName("Road New1"));
    }
}
