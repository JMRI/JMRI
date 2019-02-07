package jmri.jmrit.operations.rollingstock.cars;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Operations RollingStock Cars Kernel class Last manually cross-checked
 * on 20090131
 *
 * Still to do: Everything
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class KernelTest extends OperationsTestCase {

    @Test
    public void testKernel() {
        Kernel k1 = new Kernel("TESTKERNEL");
        Assert.assertEquals("Kernel Name", "TESTKERNEL", k1.getName());

        Car c1 = new Car("TESTCARROAD", "TESTCARNUMBER1");
        c1.setLength("40");
        c1.setWeight("1000");
        c1.setWeightTons("10");
        c1.setLoadName("L");
        Car c2 = new Car("TESTCARROAD", "TESTCARNUMBER2");
        c2.setLength("60");
        c2.setWeight("2000");
        c2.setWeightTons("20");
        c2.setLoadName("L");
        Car c3 = new Car("TESTCARROAD", "TESTCARNUMBER3");
        c3.setLength("50");
        c3.setWeight("1500");
        c3.setWeightTons("15");
        c3.setLoadName("E");

        Assert.assertEquals("Kernel Initial Length", 0, k1.getTotalLength());
        Assert.assertEquals("Kernel Initial Weight Tons", 0, k1.getAdjustedWeightTons());

        c1.setKernel(k1);
        Assert.assertEquals("Kernel Car 1 Length", 40 + Car.COUPLERS, k1.getTotalLength());
        Assert.assertEquals("Kernel Car 1 Weight Tons", 10, k1.getAdjustedWeightTons());
        Assert.assertTrue("Kernel Lead", k1.isLead(c1));
        Assert.assertTrue("Kernel Lead", c1.isLead());

        k1.add(c2);
        Assert.assertEquals("Kernel Car 2 Length", 40 + Car.COUPLERS + 60 + Car.COUPLERS, k1.getTotalLength());
        Assert.assertEquals("Kernel Car 2 Weight Tons", 30, k1.getAdjustedWeightTons());
        Assert.assertTrue("Kernel Lead", k1.isLead(c1));
        Assert.assertTrue("Kernel Lead", c1.isLead());

        k1.add(c3);
        Assert.assertEquals("Kernel Car 3 Length", 40 + Car.COUPLERS + 60 + Car.COUPLERS + 50 + Car.COUPLERS, k1.getTotalLength());
        // car 3 is empty, so only 5 tons, 15/3
        Assert.assertEquals("Kernel Car 3 Weight Tons", 35, k1.getAdjustedWeightTons());
        Assert.assertTrue("Kernel Lead", k1.isLead(c1));
        Assert.assertTrue("Kernel Lead", c1.isLead());

        k1.setLead(c2);
        Assert.assertTrue("Kernel Lead", k1.isLead(c2));
        Assert.assertFalse("Kernel Lead", k1.isLead(c1));
        Assert.assertFalse("Kernel Lead", k1.isLead(c3));

        k1.delete(c2);
        Assert.assertEquals("Kernel Car Delete 2 Length", 40 + Car.COUPLERS + 50 + Car.COUPLERS, k1.getTotalLength());
        Assert.assertEquals("Kernel Car Delete 2 Weight Tons", 15, k1.getAdjustedWeightTons());

        k1.delete(c1);
        Assert.assertEquals("Kernel Car Delete 1 Length", 50 + Car.COUPLERS, k1.getTotalLength());
        Assert.assertEquals("Kernel Car Delete 1 Weight Tons", 5, k1.getAdjustedWeightTons());

        k1.delete(c3);
        Assert.assertEquals("Kernel Car Delete 3 Length", 0, k1.getTotalLength());
        Assert.assertEquals("Kernel Car Delete 3 Weight Tons", 0, k1.getAdjustedWeightTons());

    }

    @Test
    public void testCarKernel() {
        Kernel kold = new Kernel("TESTKERNELOLD");
        Assert.assertEquals("Kernel Name old", "TESTKERNELOLD", kold.getName());

        Kernel knew = new Kernel("TESTKERNELNEW");
        Assert.assertEquals("Kernel Name new", "TESTKERNELNEW", knew.getName());

        Car c1 = new Car("TESTCARROAD", "TESTCARNUMBER1");
        c1.setLength("40");
        c1.setWeight("1000");
        Car c2 = new Car("TESTCARROAD", "TESTCARNUMBER2");
        c2.setLength("60");
        c2.setWeight("2000");
        Car c3 = new Car("TESTCARROAD", "TESTCARNUMBER3");
        c3.setLength("50");
        c3.setWeight("1500");

        //  All three cars start out in the old kernel with car 1 as the lead car.
        c1.setKernel(kold);
        c2.setKernel(kold);
        c3.setKernel(kold);
        Assert.assertEquals("Kernel Name for car 1 before", "TESTKERNELOLD", c1.getKernelName());
        Assert.assertEquals("Kernel Name for car 2 before", "TESTKERNELOLD", c2.getKernelName());
        Assert.assertEquals("Kernel Name for car 3 before", "TESTKERNELOLD", c3.getKernelName());
        Assert.assertEquals("Kernel old length before", 40 + 4 + 60 + 4 + 50 + 4, kold.getTotalLength());
        Assert.assertEquals("Kernel new length before", 0, knew.getTotalLength());
        Assert.assertTrue("Kernel old Lead is Car 1 before", kold.isLead(c1));
        Assert.assertFalse("Kernel old Lead is not Car 2 before", kold.isLead(c2));
        Assert.assertFalse("Kernel old Lead is not Car 3 before", kold.isLead(c3));
        Assert.assertFalse("Kernel new Lead is not Car 1 before", knew.isLead(c1));
        Assert.assertFalse("Kernel new Lead is not Car 2 before", knew.isLead(c2));
        Assert.assertFalse("Kernel new Lead is not Car 3 before", knew.isLead(c3));

        //  Move car 1 to the new kernel where it will be the lead car.
        //  Car 2 should now be the lead car of the old kernel.
        c1.setKernel(knew);
        Assert.assertEquals("Kernel Name for car 1 after", "TESTKERNELNEW", c1.getKernelName());
        Assert.assertEquals("Kernel Name for car 2 after", "TESTKERNELOLD", c2.getKernelName());
        Assert.assertEquals("Kernel Name for car 3 after", "TESTKERNELOLD", c3.getKernelName());
        Assert.assertEquals("Kernel old length after", 60 + 4 + 50 + 4, kold.getTotalLength());
        Assert.assertEquals("Kernel new length after", 40 + 4, knew.getTotalLength());
        Assert.assertFalse("Kernel old Lead is not Car 1 after", kold.isLead(c1));
        Assert.assertTrue("Kernel old Lead is Car 2 after", kold.isLead(c2));
        Assert.assertFalse("Kernel old Lead is not Car 3 after", kold.isLead(c3));
        Assert.assertTrue("Kernel new Lead is Car 1 after", knew.isLead(c1));
        Assert.assertFalse("Kernel new Lead is not Car 2 after", knew.isLead(c2));
        Assert.assertFalse("Kernel new Lead is not Car 3 after", knew.isLead(c3));

        //  Move car 3 to the new kernel.
        c3.setKernel(knew);
        Assert.assertEquals("Kernel Name for car 1 after3", "TESTKERNELNEW", c1.getKernelName());
        Assert.assertEquals("Kernel Name for car 2 after3", "TESTKERNELOLD", c2.getKernelName());
        Assert.assertEquals("Kernel Name for car 3 after3", "TESTKERNELNEW", c3.getKernelName());
        Assert.assertEquals("Kernel old length after3", 60 + 4, kold.getTotalLength());
        Assert.assertEquals("Kernel new length after3", 40 + 4 + 50 + 4, knew.getTotalLength());
        Assert.assertFalse("Kernel old Lead is not Car 1 after3", kold.isLead(c1));
        Assert.assertTrue("Kernel old Lead is Car 2 after3", kold.isLead(c2));
        Assert.assertFalse("Kernel old Lead is not Car 3 after3", kold.isLead(c3));
        Assert.assertTrue("Kernel new Lead is Car 1 after3", knew.isLead(c1));
        Assert.assertFalse("Kernel new Lead is not Car 2 after3", knew.isLead(c2));
        Assert.assertFalse("Kernel new Lead is not Car 3 after3", knew.isLead(c3));
    }
}
