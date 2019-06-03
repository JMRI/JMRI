package jmri.jmrix.can.cbus;

import jmri.Manager.NameValidity;
import jmri.Reporter;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * CbusReporterManagerTest.java
 *
 * Description:	tests for the CbusReporterManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 * @author	Steve Young Copyright (C) 2019 
 */
public class CbusReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    protected Object generateObjectToReport(){
        return new jmri.implementation.DefaultIdTag("ID0413276BC1", "Test Tag");
    }
    
    @Test
    public void testSingleLines() {
        Assert.assertTrue("tooltip",l.getEntryToolTip()!=null);
        Assert.assertEquals("multiple",true,l.allowMultipleAdditions("MR"));
    }
    
    @Test
    public void testvalidSystemNameFormat() {

        Assert.assertEquals("MR1",NameValidity.VALID,l.validSystemNameFormat("MR1"));
        Assert.assertEquals("MR321",NameValidity.VALID,l.validSystemNameFormat("MR321"));
        Assert.assertEquals("MR4321",NameValidity.VALID,l.validSystemNameFormat("MR4321"));
        Assert.assertEquals("MR54321",NameValidity.VALID,l.validSystemNameFormat("MR54321"));
        Assert.assertEquals("MR65535",NameValidity.VALID,l.validSystemNameFormat("MR65535"));
        Assert.assertEquals("MR+77",NameValidity.VALID,l.validSystemNameFormat("MR+77"));
        Assert.assertEquals("MR0",NameValidity.VALID,l.validSystemNameFormat("MR0"));
        
        Assert.assertEquals("MR65536",NameValidity.INVALID,l.validSystemNameFormat("MR65536"));
        Assert.assertEquals("MR-77",NameValidity.INVALID,l.validSystemNameFormat("MR-77"));
        Assert.assertEquals("M",NameValidity.INVALID,l.validSystemNameFormat("M"));
        Assert.assertEquals("R",NameValidity.INVALID,l.validSystemNameFormat("R"));
        Assert.assertEquals("MR",NameValidity.INVALID,l.validSystemNameFormat("MR"));
        Assert.assertEquals("no value",NameValidity.INVALID,l.validSystemNameFormat(""));
        Assert.assertEquals("Str ing",NameValidity.INVALID,l.validSystemNameFormat("Jon Smith"));
        Assert.assertEquals("null",NameValidity.INVALID,l.validSystemNameFormat(null));
    }
    

    @Override
    public String getSystemName(String i) {
        return "MR" + i;
    }
    
    private CanSystemConnectionMemo memo;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(new TrafficControllerScaffold());
        l = new CbusReporterManager(memo);
    }

    @After
    public void tearDown() {
        l = null;
        memo = null;
        jmri.util.JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }


}
