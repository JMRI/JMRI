package jmri.jmrix.openlcb.configurexml;

import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;
import jmri.jmrix.openlcb.OlcbSignalMast;
import jmri.jmrix.openlcb.OlcbSystemConnectionMemo;
import jmri.jmrix.openlcb.OlcbTestInterface;

import org.openlcb.AbstractConnection;
import org.openlcb.Connection;
import org.openlcb.EventID;
import org.openlcb.EventState;
import org.openlcb.Message;
import org.openlcb.NodeID;
import org.openlcb.ProducerConsumerEventReportMessage;
import org.openlcb.IdentifyConsumersMessage;
import org.openlcb.ConsumerIdentifiedMessage;
import org.openlcb.IdentifyProducersMessage;
import org.openlcb.ProducerIdentifiedMessage;
import org.openlcb.IdentifyEventsMessage;

import org.jdom2.Element;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * OlcbSignalMastXmlTest
 *
 * Description: tests for the OlcbSignalMastXml class
 *
 * @author   Bob Jacobsen Copyright (C) 2018
 */
public class OlcbSignalMastXmlTest {
        
    static OlcbSystemConnectionMemo memo;

    @Test
    public void testCtor(){
        Assert.assertNotNull("OlcbSignalMastXml constructor",new OlcbSignalMastXml());
    }

    @Test
    public void testStore(){
        OlcbSignalMast t = new OlcbSignalMast("MF$olm:AAR-1946:PL-1-high-abs($1)");
        t.setLitEventId("1.2.3.4.5.6.7.1");
        t.setNotLitEventId("1.2.3.4.5.6.7.2");
        t.setHeldEventId("1.2.3.4.5.6.7.3");
        t.setNotHeldEventId("1.2.3.4.5.6.7.4");
        t.setOutputForAppearance("Clear", "1.2.3.4.5.6.7.10");
        t.setOutputForAppearance("Approach", "1.2.3.4.5.6.7.11");
        t.setOutputForAppearance("Permissive", "1.2.3.4.5.6.7.12");
        t.setOutputForAppearance("Stop", "1.2.3.4.5.6.7.13");
        
        OlcbSignalMastXml x = new OlcbSignalMastXml();
        
        Element e = x.store(t);
        Assert.assertNotNull("Element", e);
        
        Assert.assertEquals("1.2.3.4.5.6.7.1", e.getChild("lit").getChild("lit").getValue());
        Assert.assertEquals("1.2.3.4.5.6.7.2", e.getChild("lit").getChild("notlit").getValue());
        Assert.assertEquals("1.2.3.4.5.6.7.3", e.getChild("held").getChild("held").getValue());
        Assert.assertEquals("1.2.3.4.5.6.7.4", e.getChild("held").getChild("notheld").getValue());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @BeforeClass
    public static void preClassInit() {
        JUnitUtil.setUp();
        memo = OlcbTestInterface.createForLegacyTests();
    }

    @AfterClass
    public static void postClassTearDown() {
        if(memo != null && memo.getInterface() !=null ) {
           memo.getInterface().dispose();
        }
        memo = null;
        JUnitUtil.tearDown();
    }

}

