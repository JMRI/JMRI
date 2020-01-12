package jmri.jmrit.operations.rollingstock.cars;

import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Operations RollingStock Cars class Last manually cross-checked
 * on 20090131
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class CarOwnersTest extends OperationsTestCase {

    @Test
    public void testAddAndDeleteCarOwners() {
        CarOwners co1 = InstanceManager.getDefault(CarOwners.class);

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
}
