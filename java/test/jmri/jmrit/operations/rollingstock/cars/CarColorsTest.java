package jmri.jmrit.operations.rollingstock.cars;

import javax.swing.JComboBox;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;

/**
 * Tests for the Operations RollingStock CarColors class Last manually
 * cross-checked on 20090131
 * <p>
 * Still to do: Everything
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
 */
public class CarColorsTest extends OperationsTestCase {

    @Test
    public void testDefaultCarColors() {
        CarColors cc1 = InstanceManager.getDefault(CarColors.class);
        cc1.getNames(); // load predefined colors

        Assert.assertTrue("Car Color Predefined Red", cc1.containsName("Red"));
        Assert.assertTrue("Car Color Predefined Blue", cc1.containsName("Blue"));
    }

    @Test
    public void testAddAndDeleteCarColors() {
        CarColors cc1 = InstanceManager.getDefault(CarColors.class);
        String[] colors = cc1.getNames(); // load predefined colors
        Assert.assertEquals("default number of colors", 12, colors.length);
        cc1.addName("BoxCar Red");
        Assert.assertTrue("Car Color Add", cc1.containsName("BoxCar Red"));
        Assert.assertFalse("Car Color Never Added Dirty Blue", cc1.containsName("Dirty Blue"));
        cc1.addName("Ugly Brown");
        Assert.assertTrue("Car Color Still Has BoxCar Red", cc1.containsName("BoxCar Red"));
        Assert.assertTrue("Car Color Add Ugly Brown", cc1.containsName("Ugly Brown"));
        // get sorted list
        colors = cc1.getNames();
        Assert.assertEquals("First color name", "Ugly Brown", colors[11]);
        Assert.assertEquals("2nd color name", "BoxCar Red", colors[2]);
        JComboBox<?> box = cc1.getComboBox();
        Assert.assertEquals("First comboBox color name", "Ugly Brown", box.getItemAt(11));
        Assert.assertEquals("2nd comboBox color name", "BoxCar Red", box.getItemAt(2));
        cc1.deleteName("Ugly Brown");
        Assert.assertFalse("Car Color Delete Ugly Brown", cc1.containsName("Ugly Brown"));
        cc1.deleteName("BoxCar Red");
        Assert.assertFalse("Car Color Delete BoxCar Red", cc1.containsName("BoxCar Red"));
    }
}
