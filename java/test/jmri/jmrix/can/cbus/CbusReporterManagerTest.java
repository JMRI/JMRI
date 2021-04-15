package jmri.jmrix.can.cbus;

import java.util.List;

import jmri.*;
import jmri.Manager.NameValidity;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * CbusReporterManagerTest.java
 *
 * Test for the CbusReporterManager class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 * @author Steve Young Copyright (C) 2019 
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
    }
    

    @Override
    public String getSystemName(String i) {
        return "MR" + i;
    }
    
    @Test
    @Override
    public void testAutoSystemNames() {
        Assert.assertEquals("No auto system names",0,tcis.numListeners());
    }
    
    @Test
    public void testGetSetDefaultTimeout() {
        Assert.assertEquals("Default timeout",2000,((CbusReporterManager) l).getTimeout());
        ((CbusReporterManager) l).setTimeout(5);
        Assert.assertEquals("New timeout 5",5,((CbusReporterManager) l).getTimeout());
    }
    
    @Test
    public void testGetKnownBeanProperties() {
    
        List<NamedBeanPropertyDescriptor<?>> cbrepproplist =  l.getKnownBeanProperties();
        Assert.assertEquals("2 properties at present",2,cbrepproplist.size());
        
        NamedBeanPropertyDescriptor<?> nbpd = cbrepproplist.get(0);
        Assert.assertEquals("Column Header matches descriptor key",CbusReporterManager.CBUS_REPORTER_DESCRIPTOR_KEY,nbpd.getColumnHeaderText());
        Assert.assertEquals("Editable if CBUS Reporter",true,nbpd.isEditable(l.provideReporter("123")));
        Assert.assertEquals("Not Editable if null",false,nbpd.isEditable(null));
        Assert.assertEquals("Default reporter type set in properties",CbusReporterManager.CBUS_DEFAULT_REPORTER_TYPE,nbpd.defaultValue);
        Assert.assertEquals("reporter property key set",CbusReporterManager.CBUS_REPORTER_DESCRIPTOR_KEY,nbpd.propertyKey);

        Assert.assertEquals("Currently 2 options",2,((SelectionPropertyDescriptor)nbpd).getOptions().length);
        Assert.assertEquals("Currently 2 option tooltips",2,((SelectionPropertyDescriptor)nbpd).getOptionToolTips().size());
        
        nbpd = cbrepproplist.get(1);
        Assert.assertEquals("Column Header matches sensor follower descriptor key",CbusReporterManager.CBUS_MAINTAIN_SENSOR_DESCRIPTOR_KEY,nbpd.getColumnHeaderText());
        Assert.assertEquals("sensor follower Editable if CBUS Reporter",true,nbpd.isEditable(l.provideReporter("123")));
        Assert.assertEquals("sensor follower Not Editable if null",false,nbpd.isEditable(null));
        Assert.assertFalse("Default reporter sensor follower set in properties", (Boolean) nbpd.defaultValue);
        Assert.assertEquals("sensor follower key set",CbusReporterManager.CBUS_MAINTAIN_SENSOR_DESCRIPTOR_KEY,nbpd.propertyKey);
        
        Assert.assertFalse("Equals different",cbrepproplist.get(0).equals(nbpd));
        
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testNoDuplicatePropertiesInMultipleConnections() {
    
        CanSystemConnectionMemo otherMemo = new CanSystemConnectionMemo("M2");
        otherMemo.setUserName("CAN2");
        CbusReporterManager ll = new CbusReporterManager(otherMemo);
        
        InstanceManager.setReporterManager(l);
        ReporterManager reporterManager = InstanceManager.getDefault(jmri.ReporterManager.class);
        
        ProxyManager<Reporter> proxy = (ProxyManager<Reporter>) reporterManager;
        
        Assert.assertEquals("2 Managers found, l + Internal",2,proxy.getManagerList().size());
        Assert.assertEquals("2 properties found",2,proxy.getKnownBeanProperties().size());

        proxy.addManager(ll);
        
        Assert.assertEquals("3 Managers found, I, l, ll",3,proxy.getManagerList().size());
        Assert.assertTrue("M in list",proxy.getManagerList().contains(l));
        Assert.assertTrue("M2 in list",proxy.getManagerList().contains(ll));
        Assert.assertEquals("Still 2 properties found",2,proxy.getKnownBeanProperties().size());
        
        ll.dispose();
        otherMemo.dispose();
    }
    
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        l = new CbusReporterManager(memo);
    }

    @AfterEach
    public void tearDown() {
        l = null;
        tcis.terminateThreads();
        tcis = null;
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

}
