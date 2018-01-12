package jmri.jmrix.openlcb;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the OlcbSignalMast implementation
 *
 * @author	Bob Jacobsen Copyright (C) 2013, 2017, 2018
 * updated to JUnit4 2016
 */
public class OlcbSignalMastTest {

    @Test
    public void testCtor1() {
        OlcbSignalMast t = new OlcbSignalMast("MF$olm:AAR-1946:PL-1-high-abs(1)");

        Assert.assertEquals("system name", "MF$olm:AAR-1946:PL-1-high-abs(1)", t.getSystemName());
    }

    @Test
    public void testStopAspect() {
        OlcbSignalMast t = new OlcbSignalMast("MF$olm:AAR-1946:PL-1-high-abs(1)");
        t.setOutputForAppearance("Stop", "1.2.3.4.5.6.7.8");

        Assert.assertEquals("Stop aspect event", "x0102030405060708", t.getOutputForAppearance("Stop"));
    }

    @Test
    public void testSetGetEvents() {
        OlcbSignalMast t = new OlcbSignalMast("MF$olm:AAR-1946:PL-1-high-abs(1)");
        
        t.setLitEventId("1.2.3.4.5.6.7.1");
        Assert.assertEquals("lit", "x0102030405060701", t.getLitEventId());
        t.setNotLitEventId("1.2.3.4.5.6.7.2");
        Assert.assertEquals("not lit", "x0102030405060702", t.getNotLitEventId());

        t.setHeldEventId("1.2.3.4.5.6.7.3");
        Assert.assertEquals("held", "x0102030405060703", t.getHeldEventId());
        t.setNotHeldEventId("1.2.3.4.5.6.7.4");
        Assert.assertEquals("lit", "x0102030405060704", t.getNotHeldEventId());  
    }

    @Test
    public void testLitStateTransitions() {
        OlcbSignalMast t = new OlcbSignalMast("MF$olm:AAR-1946:PL-1-high-abs(1)");

        t.setLitEventId("1.2.3.4.5.6.7.1");
        t.setNotLitEventId("1.2.3.4.5.6.7.2");
        
        t.consumeEvent(new OlcbAddress("1.2.3.4.5.6.7.1"));
        Assert.assertEquals("lit true", true, t.getLit());         
        // and check the IdentifyConsumers result
        
        
        t.consumeEvent(new OlcbAddress("1.2.3.4.5.6.7.2"));
        Assert.assertEquals("lit false", true, t.getLit());
        // and check the IdentifyConsumers result
        
        
                 
    }
 
    // from here down is testing infrastructure

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        
        new OlcbSystemConnectionMemo(); // this self-registers as 'M'
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
