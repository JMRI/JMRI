package jmri.jmrit.operations.rollingstock.cars;

import javax.swing.JComboBox;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;


public class KernelManagerTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        KernelManager manager = InstanceManager.getDefault(KernelManager.class);
        Assert.assertNotNull("Manager Creation", manager);
    }

    @Test
    public void testKernel() {
        KernelManager km = InstanceManager.getDefault(KernelManager.class);
        Kernel k = km.newKernel(KernelManager.NONE);
        Assert.assertNull(k);

        k = km.newKernel("A");
        Assert.assertNotNull(k);

        Kernel kt = km.newKernel("A");
        Assert.assertNotNull(kt);
        Assert.assertEquals("Same kernel", k, kt);
        
        // test delete kernel
        Car c = InstanceManager.getDefault(CarManager.class).newRS("SP", "1");
        c.setKernel(kt);

        km.deleteKernel("A");
        kt = km.getKernelByName("A");
        Assert.assertNull(kt);
        Assert.assertNull(c.getKernel());
    }

    @Test
    public void testKernelComboBox() {
        JComboBox<String> cb = InstanceManager.getDefault(KernelManager.class).getComboBox();
        Assert.assertEquals("Number of items", 1, cb.getItemCount());
        Assert.assertEquals("Empty", CarManager.NONE, cb.getSelectedItem());

        InstanceManager.getDefault(KernelManager.class).newKernel("C");
        InstanceManager.getDefault(KernelManager.class).newKernel("B");
        InstanceManager.getDefault(KernelManager.class).newKernel("A");
        cb = InstanceManager.getDefault(KernelManager.class).getComboBox();

        Assert.assertEquals("Number of items", 4, cb.getItemCount());
        Assert.assertEquals("Empty", KernelManager.NONE, cb.getSelectedItem());

        Assert.assertEquals("1st item", "A", cb.getItemAt(1));
        Assert.assertEquals("1st item", "B", cb.getItemAt(2));
        Assert.assertEquals("1st item", "C", cb.getItemAt(3));
    }
    
    @Test
    public void testKernelNameLength() {
        KernelManager km = InstanceManager.getDefault(KernelManager.class);
        Assert.assertEquals("No kernels", 0, km.getMaxNameLength());
        
        km.newKernel("A");
        Assert.assertEquals("1 kernel", 1, km.getMaxNameLength());
        
        km.newKernel("ABC");
        Assert.assertEquals("2 kernel", 3, km.getMaxNameLength());
    }
    
    @Test
    public void testReplaceKenelName() {
        KernelManager km = InstanceManager.getDefault(KernelManager.class);
        
        Kernel k = km.newKernel("A");
        Assert.assertNotNull(k);
        
        Car c1 = InstanceManager.getDefault(CarManager.class).newRS("SP", "1");
        Car c2 = InstanceManager.getDefault(CarManager.class).newRS("SP", "2");
        c1.setKernel(k);
        c2.setKernel(k);
        Assert.assertEquals("Kernel name", "A", c1.getKernelName());
        Assert.assertEquals("Kernel name", "A", c2.getKernelName());
        Assert.assertTrue(c1.isLead());
        Assert.assertFalse(c2.isLead());

        km.replaceKernelName("A", "B");
        Kernel kt = km.getKernelByName("B");
        Assert.assertNotNull(kt);
        // Replace when test was created doesn't delete the old kernel
        // GUI does a replace followed by a delete
        k = km.getKernelByName("A");
        Assert.assertNotNull(k);
        
        Assert.assertEquals("Kernel name", "B", c1.getKernelName());
        Assert.assertEquals("Kernel name", "B", c2.getKernelName());
        Assert.assertTrue(c1.isLead());
        Assert.assertFalse(c2.isLead());
    }
}
