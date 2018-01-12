package jmri.jmrix.openlcb.configurexml;

import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;
import jmri.jmrix.openlcb.OlcbSignalMast;

import org.jdom2.Element;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * OlcbSignalMastXmlTest
 *
 * Description: tests for the OlcbSignalMastXml class
 *
 * @author   Bob Jacobsen Copyright (C) 2018
 */
public class OlcbSignalMastXmlTest {

    @Test
    public void testCtor(){
        Assert.assertNotNull("OlcbSignalMastXml constructor",new OlcbSignalMastXml());
    }

    @Test
    public void testStore(){
        OlcbSignalMast t = new OlcbSignalMast("MF$olm:AAR-1946:PL-1-high-abs(1)");
        t.setLitEventId("1.2.3.4.5.6.7.1");
        t.setNotLitEventId("1.2.3.4.5.6.7.2");
        t.setHeldEventId("1.2.3.4.5.6.7.3");
        t.setNotHeldEventId("1.2.3.4.5.6.7.4");
        t.setOutputForAppearance("Clear", "1.2.3.4.5.6.7.10");
        t.setOutputForAppearance("Approach", "1.2.3.4.5.6.7.11");
        t.setOutputForAppearance("Permissive", "1.2.3.4.5.6.7.12");
        t.setOutputForAppearance("Stop", "1.2.3.4.5.6.7.13");
        
        JUnitAppender.assertErrorMessage("No OpenLCB connection found for system prefix \"M\", so mast \"MF$olm:AAR-1946:PL-1-high-abs(1)\" will not function");
        
        OlcbSignalMastXml x = new OlcbSignalMastXml();
        
        Element e = x.store(t);
        Assert.assertNotNull("Element", e);
        
        Assert.assertEquals("x0102030405060701", e.getChild("lit").getChild("lit").getValue());
        Assert.assertEquals("x0102030405060702", e.getChild("lit").getChild("notlit").getValue());
        Assert.assertEquals("x0102030405060703", e.getChild("held").getChild("held").getValue());
        Assert.assertEquals("x0102030405060704", e.getChild("held").getChild("notheld").getValue());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

