package jmri.configurexml;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.*;
import jmri.managers.DefaultTransitManager;
import jmri.managers.configurexml.DefaultTransitManagerXml;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for TransitManagerXml class.
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2016
 **/

public class TransitManagerXmlTest {

    @Test
    public void testCtor(){
        assertNotNull( new DefaultTransitManagerXml(), "Constructor");
    }

    @Test
    public void testNoElementIfEmpty(){
        var tmx = new DefaultTransitManagerXml();
        TransitManager tm = new DefaultTransitManager();
        assertNull( tmx.store(tm), "No elements");
    }

    @Test
    public void testStoreOneTransit() {
        var tmx = new DefaultTransitManagerXml();
        TransitManager tm = new DefaultTransitManager();
        Transit t = tm.createNewTransit("TS1", "user");

        Section s = new jmri.implementation.DefaultSection("SS1");
        TransitSection ts = new TransitSection(s,0,0,false);

        TransitSectionAction ta = new TransitSectionAction(0,0);
        ts.addAction(ta);

        t.addTransitSection(ts);

        org.jdom2.Element e = tmx.store(tm);
        assertNotNull( e, "Element(s) returned" );

        assertNotNull( tmx.load(e, null), "Element(s) processed");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown(){
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
