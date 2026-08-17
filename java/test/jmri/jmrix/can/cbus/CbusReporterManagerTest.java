package jmri.jmrix.can.cbus;

import java.util.List;

import jmri.*;
import jmri.Manager.NameValidity;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

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
        assertNotNull(l.getEntryToolTip(),"tooltip");
        assertTrue(l.allowMultipleAdditions("MR"),"multiple");
    }

    @Test
    public void testvalidSystemNameFormat() {

        assertEquals(NameValidity.VALID,l.validSystemNameFormat("MR1"),"MR1");
        assertEquals(NameValidity.VALID,l.validSystemNameFormat("MR321"),"MR321");
        assertEquals(NameValidity.VALID,l.validSystemNameFormat("MR4321"),"MR4321");
        assertEquals(NameValidity.VALID,l.validSystemNameFormat("MR54321"),"MR54321");
        assertEquals(NameValidity.VALID,l.validSystemNameFormat("MR65535"),"MR65535");
        assertEquals(NameValidity.VALID,l.validSystemNameFormat("MR+77"),"MR+77");
        assertEquals(NameValidity.VALID,l.validSystemNameFormat("MR0"),"MR0");

        assertEquals(NameValidity.INVALID,l.validSystemNameFormat("MR65536"),"MR65536");
        assertEquals(NameValidity.INVALID,l.validSystemNameFormat("MR-77"),"MR-77");
        assertEquals(NameValidity.INVALID,l.validSystemNameFormat("M"),"M");
        assertEquals(NameValidity.INVALID,l.validSystemNameFormat("R"),"R");
        assertEquals(NameValidity.INVALID,l.validSystemNameFormat("MR"),"MR");
        assertEquals(NameValidity.INVALID,l.validSystemNameFormat(""),"no value");
        assertEquals(NameValidity.INVALID,l.validSystemNameFormat("Jon Smith"),"Str ing");
    }


    @Override
    public String getSystemName(String i) {
        return "MR" + i;
    }

    @Test
    @Override
    public void testAutoSystemNames() {
        assertNotNull(tcis);
        assertEquals(1,tcis.numListeners(),"No auto system names");
    }

    @Test
    public void testGetSetDefaultTimeout() {
        assertEquals(2000,((CbusReporterManager) l).getTimeout(),"Default timeout");
        ((CbusReporterManager) l).setTimeout(5);
        assertEquals(5,((CbusReporterManager) l).getTimeout(),"New timeout 5");
    }

    @Test
    public void testGetKnownBeanProperties() {

        List<NamedBeanPropertyDescriptor<?>> cbrepproplist =  l.getKnownBeanProperties();
        assertEquals(2,cbrepproplist.size(),"2 properties at present");

        NamedBeanPropertyDescriptor<?> nbpd = cbrepproplist.get(0);
        assertEquals(CbusReporterManager.CBUS_REPORTER_DESCRIPTOR_KEY,nbpd.getColumnHeaderText(),
                "Column Header matches descriptor key");
        assertTrue(nbpd.isEditable(l.provideReporter("123")),"Editable if CBUS Reporter");
        assertFalse(nbpd.isEditable(null),"Not Editable if null");
        assertEquals(CbusReporterManager.CBUS_DEFAULT_REPORTER_TYPE,nbpd.defaultValue,
                "Default reporter type set in properties");
        assertEquals(CbusReporterManager.CBUS_REPORTER_DESCRIPTOR_KEY,nbpd.propertyKey,
                "reporter property key set");

        assertEquals(2,((SelectionPropertyDescriptor)nbpd).getOptions().length,"Currently 2 options");
        assertEquals(2,((SelectionPropertyDescriptor)nbpd).getOptionToolTips().size(),"Currently 2 option tooltips");

        nbpd = cbrepproplist.get(1);
        assertEquals(CbusReporterManager.CBUS_MAINTAIN_SENSOR_DESCRIPTOR_KEY,nbpd.getColumnHeaderText(),
                "Column Header matches sensor follower descriptor key");
        assertTrue(nbpd.isEditable(l.provideReporter("123")),"sensor follower Editable if CBUS Reporter");
        assertFalse(nbpd.isEditable(null),"sensor follower Not Editable if null");
        assertFalse((Boolean) nbpd.defaultValue,"Default reporter sensor follower set in properties");
        assertEquals(CbusReporterManager.CBUS_MAINTAIN_SENSOR_DESCRIPTOR_KEY,nbpd.propertyKey,
                "sensor follower key set");

        assertNotEquals(cbrepproplist.get(0),nbpd,"Equals different");

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNoDuplicatePropertiesInMultipleConnections() {

        CanSystemConnectionMemo otherMemo = new CanSystemConnectionMemo("M2");
        otherMemo.setUserName("CAN2");
        var otherTcis = new TrafficControllerScaffold();
        otherMemo.setTrafficController(otherTcis);

        CbusReporterManager ll = new CbusReporterManager(otherMemo);

        InstanceManager.setReporterManager(l);
        ReporterManager reporterManager = InstanceManager.getDefault(jmri.ReporterManager.class);

        ProxyManager<Reporter> proxy = (ProxyManager<Reporter>) reporterManager;

        assertEquals(2,proxy.getManagerList().size(),"2 Managers found, l + Internal");
        assertEquals(2,proxy.getKnownBeanProperties().size(),"2 properties found");

        proxy.addManager(ll);

        assertEquals(3,proxy.getManagerList().size(),"3 Managers found, I, l, ll");
        assertTrue(proxy.getManagerList().contains(l),"M in list");
        assertTrue(proxy.getManagerList().contains(ll),"M2 in list");
        assertEquals(2,proxy.getKnownBeanProperties().size(),"Still 2 properties found");

        ll.dispose();
        otherTcis.terminateThreads();
        otherMemo.dispose();
    }

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis = null;

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
        Assertions.assertNotNull(tcis);
        tcis.terminateThreads();
        tcis = null;
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

}
