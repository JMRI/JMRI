package jmri.jmrit.operations.locations.divisions;

import javax.swing.JComboBox;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;

/**
*
* @author Daniel Boudreau Copyright (C) 2021
*/
public class DivisionManagerTest extends OperationsTestCase {

    @Test
    public void testDivisionManager() {
        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        Division d = dm.newDivision("new test division");
        Assert.assertNotNull(d);
    }

    @Test
    public void testDivisionComboBoxes() {
        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        Division da = dm.newDivision("divisionA");
        Division db = dm.newDivision("divisionB");
        Division dc = dm.newDivision("divisionC");
        JComboBox<Division> box = dm.getComboBox();
        Assert.assertEquals("1st", null, box.getItemAt(0));
        Assert.assertEquals("2nd", da, box.getItemAt(1));
        Assert.assertEquals("3rd", db, box.getItemAt(2));
        Assert.assertEquals("4th", dc, box.getItemAt(3));
    }
}
