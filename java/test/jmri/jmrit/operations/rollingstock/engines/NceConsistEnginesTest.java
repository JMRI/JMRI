package jmri.jmrit.operations.rollingstock.engines;

import java.util.ArrayList;
import java.util.List;
import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations RollingStock Engine class Last manually
 * cross-checked on 20090131
 *
 * Still to do: Engine: Destination Engine: Verify everything else EngineTypes:
 * get/set Names lists EngineModels: get/set Names lists EngineLengths:
 * Everything Consist: Everything Import: Everything EngineManager: Engine
 * register/deregister EngineManager: Consists
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class NceConsistEnginesTest extends OperationsTestCase {

    public void testConsist() {
        Consist c1 = new Consist("TESTCONSIST");
        Assert.assertEquals("Consist Name", "TESTCONSIST", c1.getName());

        Engine e1 = new Engine("TESTROAD", "TESTNUMBER1");
        e1.setModel("GP35");  //  e1.setLength("56");
        Engine e2 = new Engine("TESTROAD", "TESTNUMBER2");
        e2.setModel("GP40");  //  e2.setLength("59");
        Engine e3 = new Engine("TESTROAD", "TESTNUMBER3");
        e3.setModel("SW1500");  //  e3.setLength("45");
        Engine e4 = new Engine("TESTROAD", "TESTNUMBER4");
        e4.setModel("SW1500");  //  e3.setLength("45");

        Assert.assertEquals("Consist Initial Length", 0, c1.getTotalLength());
        Assert.assertFalse("Consist Lead Engine 0", c1.isLead(e1));

        c1.add(e1);
        Assert.assertEquals("Consist Engine 1 Length", 56 + 4, c1.getTotalLength());
        Assert.assertTrue("Consist Lead Engine 1", c1.isLead(e1));

        c1.add(e2);
        Assert.assertEquals("Consist Engine 2 Length", 56 + 4 + 59 + 4, c1.getTotalLength());
        Assert.assertTrue("Consist Lead Engine 1 after 2", c1.isLead(e1));

        c1.setLead(e2);
        Assert.assertFalse("Consist Lead Engine 1 after 2c", c1.isLead(e1));
        Assert.assertTrue("Consist Lead Engine 2 after 2c", c1.isLead(e2));

        c1.add(e3);
        Assert.assertEquals("Consist Engine 3 Length", 56 + 4 + 59 + 4 + 45 + 4, c1.getTotalLength());
        Assert.assertTrue("Consist Lead Engine 2 after 3", c1.isLead(e2));
        Assert.assertFalse("Consist Lead Engine 1 after 3", c1.isLead(e1));
        Assert.assertFalse("Consist Lead Engine 3 after 3", c1.isLead(e3));

        // Can't set lead engine if not part of consist
        c1.setLead(e4);
        Assert.assertTrue("Consist Lead Engine 2 after 4c", c1.isLead(e2));
        Assert.assertFalse("Consist Lead Engine 4 after 4c", c1.isLead(e4));
        List<Engine> tempengines = new ArrayList<Engine>();
        tempengines = c1.getEngines();
        Assert.assertTrue("Consist Engine 2 after 4c", tempengines.contains(e2));
        Assert.assertFalse("Consist Engine 4 after 4c", tempengines.contains(e4));
     
        c1.delete(e2);
        Assert.assertEquals("Consist Engine Delete 2 Length", 56 + 4 + 45 + 4, c1.getTotalLength());
        
        c1.delete(e1);
        Assert.assertEquals("Consist Engine Delete 1 Length", 45 + 4, c1.getTotalLength());
        
        c1.delete(e3);
        Assert.assertEquals("Consist Engine Delete 3 Length", 0, c1.getTotalLength());
        
    }

    public void testEngineConsist() {
        Consist cold = new Consist("TESTCONSISTOLD");
        Assert.assertEquals("Consist Name old", "TESTCONSISTOLD", cold.getName());

        Consist cnew = new Consist("TESTCONSISTNEW");
        Assert.assertEquals("Consist Name new", "TESTCONSISTNEW", cnew.getName());

        Engine e1 = new Engine("TESTROAD", "TESTNUMBER1");
        e1.setModel("GP35");  //  e1.setLength("56");
        Engine e2 = new Engine("TESTROAD", "TESTNUMBER2");
        e2.setModel("GP40");  //  e2.setLength("59");
        Engine e3 = new Engine("TESTROAD", "TESTNUMBER3");
        e3.setModel("SW1500");  //  e3.setLength("45");
        Engine e4 = new Engine("TESTROAD", "TESTNUMBER4");
        e4.setModel("SW1500");  //  e3.setLength("45");

        //  All three engines start out in the old consist with engine 1 as the lead engine.
        e1.setConsist(cold);
        e2.setConsist(cold);
        e3.setConsist(cold);
        Assert.assertEquals("Consist Name for engine 1 before", "TESTCONSISTOLD", e1.getConsistName());
        Assert.assertEquals("Consist Name for engine 2 before", "TESTCONSISTOLD", e2.getConsistName());
        Assert.assertEquals("Consist Name for engine 3 before", "TESTCONSISTOLD", e3.getConsistName());
        Assert.assertEquals("Consist old length before", 56 + 4 + 59 + 4 + 45 + 4, cold.getTotalLength());
        Assert.assertEquals("Consist new length before", 0, cnew.getTotalLength());
        Assert.assertTrue("Consist old Lead is Engine 1 before", cold.isLead(e1));
        Assert.assertFalse("Consist old Lead is not Engine 2 before", cold.isLead(e2));
        Assert.assertFalse("Consist old Lead is not Engine 3 before", cold.isLead(e3));
        Assert.assertFalse("Consist new Lead is not Engine 1 before", cnew.isLead(e1));
        Assert.assertFalse("Consist new Lead is not Engine 2 before", cnew.isLead(e2));
        Assert.assertFalse("Consist new Lead is not Engine 3 before", cnew.isLead(e3));

        //  Move engine 1 to the new consist where it will be the lead engine.
        //  Engine 2 should now be the lead engine of the old consist.
        e1.setConsist(cnew);
        Assert.assertEquals("Consist Name for engine 1 after", "TESTCONSISTNEW", e1.getConsistName());
        Assert.assertEquals("Consist Name for engine 2 after", "TESTCONSISTOLD", e2.getConsistName());
        Assert.assertEquals("Consist Name for engine 3 after", "TESTCONSISTOLD", e3.getConsistName());
        Assert.assertEquals("Consist old length after", 59 + 4 + 45 + 4, cold.getTotalLength());
        Assert.assertEquals("Consist new length after", 56 + 4, cnew.getTotalLength());
        Assert.assertFalse("Consist old Lead is not Engine 1 after", cold.isLead(e1));
        Assert.assertTrue("Consist old Lead is Engine 2 after", cold.isLead(e2));
        Assert.assertFalse("Consist old Lead is not Engine 3 after", cold.isLead(e3));
        Assert.assertTrue("Consist new Lead is Engine 1 after", cnew.isLead(e1));
        Assert.assertFalse("Consist new Lead is not Engine 2 after", cnew.isLead(e2));
        Assert.assertFalse("Consist new Lead is not Engine 3 after", cnew.isLead(e3));

        //  Move engine 3 to the new consist.
        e3.setConsist(cnew);
        Assert.assertEquals("Consist Name for engine 1 after3", "TESTCONSISTNEW", e1.getConsistName());
        Assert.assertEquals("Consist Name for engine 2 after3", "TESTCONSISTOLD", e2.getConsistName());
        Assert.assertEquals("Consist Name for engine 3 after3", "TESTCONSISTNEW", e3.getConsistName());
        Assert.assertEquals("Consist old length after3", 59 + 4, cold.getTotalLength());
        Assert.assertEquals("Consist new length after3", 56 + 4 + 45 + 4, cnew.getTotalLength());
        Assert.assertFalse("Consist old Lead is not Engine 1 after3", cold.isLead(e1));
        Assert.assertTrue("Consist old Lead is Engine 2 after3", cold.isLead(e2));
        Assert.assertFalse("Consist old Lead is not Engine 3 after3", cold.isLead(e3));
        Assert.assertTrue("Consist new Lead is Engine 1 after3", cnew.isLead(e1));
        Assert.assertFalse("Consist new Lead is not Engine 2 after3", cnew.isLead(e2));
        Assert.assertFalse("Consist new Lead is not Engine 3 after3", cnew.isLead(e3));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public NceConsistEnginesTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", NceConsistEnginesTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NceConsistEnginesTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
       super.tearDown();
    }


}
