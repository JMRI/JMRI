package jmri.jmrit.operations.rollingstock.engines;

import javax.swing.JComboBox;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;


public class ConsistManagerTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ConsistManager manager = InstanceManager.getDefault(ConsistManager.class);
        Assert.assertNotNull("Manager Creation", manager);
    }

    @Test
    public void testConsist() {
        EngineManager em = InstanceManager.getDefault(EngineManager.class);
        ConsistManager cm = InstanceManager.getDefault(ConsistManager.class);
        Consist c = cm.newConsist(ConsistManager.NONE);
        Assert.assertNull(c);

        c = cm.newConsist("A");
        Assert.assertNotNull(c);

        Consist ct = cm.newConsist("A");
        Assert.assertNotNull(ct);
        Assert.assertEquals("Same consist", c, ct);

        cm.replaceConsistName("A", "B");
        ct = cm.getConsistByName("B");
        Assert.assertNotNull(ct);
        // Replace when test was created doesn't delete the old consist
        // GUI does a replace followed by a delete
        c = cm.getConsistByName("A");
        Assert.assertNotNull(c);
        
        // test delete consist
        Engine e = em.newRS("SP", "1");
        e.setConsist(ct);

        InstanceManager.getDefault(ConsistManager.class).deleteConsist("B");
        ct = cm.getConsistByName("B");
        Assert.assertNull(ct);
        Assert.assertNull(e.getConsist());
    }
    
    @Test
    public void testConsistComboBox() {
        JComboBox<String> cb = InstanceManager.getDefault(ConsistManager.class).getComboBox();
        Assert.assertEquals("Number of items", 1, cb.getItemCount());
        Assert.assertEquals("Empty", EngineManager.NONE, cb.getSelectedItem());

        InstanceManager.getDefault(ConsistManager.class).newConsist("C");
        InstanceManager.getDefault(ConsistManager.class).newConsist("B");
        InstanceManager.getDefault(ConsistManager.class).newConsist("A");
        cb = InstanceManager.getDefault(ConsistManager.class).getComboBox();

        Assert.assertEquals("Number of items", 4, cb.getItemCount());
        Assert.assertEquals("Empty", ConsistManager.NONE, cb.getSelectedItem());

        Assert.assertEquals("1st item", "A", cb.getItemAt(1));
        Assert.assertEquals("1st item", "B", cb.getItemAt(2));
        Assert.assertEquals("1st item", "C", cb.getItemAt(3));
    }
    
    @Test
    public void testConsistNameLength() {
        ConsistManager km = InstanceManager.getDefault(ConsistManager.class);
        Assert.assertEquals("No consists", 0, km.getMaxNameLength());
        
        km.newConsist("A");
        Assert.assertEquals("1 consist", 1, km.getMaxNameLength());
        
        km.newConsist("ABC");
        Assert.assertEquals("2 consist", 3, km.getMaxNameLength());
    }
    
    @Test
    public void testReplaceKenelName() {
        ConsistManager km = InstanceManager.getDefault(ConsistManager.class);
        
        Consist k = km.newConsist("A");
        Assert.assertNotNull(k);
        
        Engine e1 = InstanceManager.getDefault(EngineManager.class).newRS("SP", "1");
        Engine e2 = InstanceManager.getDefault(EngineManager.class).newRS("SP", "2");
        e1.setConsist(k);
        e2.setConsist(k);
        Assert.assertEquals("Consist name", "A", e1.getConsistName());
        Assert.assertEquals("Consist name", "A", e2.getConsistName());
        Assert.assertTrue(e1.isLead());
        Assert.assertFalse(e2.isLead());

        km.replaceConsistName("A", "B");
        Consist kt = km.getConsistByName("B");
        Assert.assertNotNull(kt);
        // Replace when test was created doesn't delete the old consist
        // GUI does a replace followed by a delete
        k = km.getConsistByName("A");
        Assert.assertNotNull(k);
        
        Assert.assertEquals("Consist name", "B", e1.getConsistName());
        Assert.assertEquals("Consist name", "B", e2.getConsistName());
        Assert.assertTrue(e1.isLead());
        Assert.assertFalse(e2.isLead());
    }
}
