package jmri.jmrit.operations.locations.divisions;

import java.util.List;

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
    
    @Test
    public void testDivisionLists() {
        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        Division dc = dm.newDivision("divisionC");
        Division db = dm.newDivision("divisionB");
        Division da = dm.newDivision("divisionA");
        
        List<Division> list = dm.getDivisionsByIdList();
        Assert.assertEquals("1st", dc ,list.get(0));
        Assert.assertEquals("2nd", db, list.get(1));
        Assert.assertEquals("3rd", da, list.get(2));
        
        list = dm.getDivisionsByNameList();
        Assert.assertEquals("1st", da ,list.get(0));
        Assert.assertEquals("2nd", db, list.get(1));
        Assert.assertEquals("3rd", dc, list.get(2));
    }
    
    @Test
    public void testDivisionCreation() {
        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        Division dc = dm.newDivision("divisionA"); // use the same name twice
        Division db = dm.newDivision("divisionB");
        Division da = dm.newDivision("divisionA");
        
        Assert.assertEquals("Id", "1", da.getId());
        Assert.assertEquals("Id", "2", db.getId());
        Assert.assertEquals("Id", "1", dc.getId());
    }
    
    
}
