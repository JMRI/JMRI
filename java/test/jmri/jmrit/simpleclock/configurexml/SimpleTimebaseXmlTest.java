package jmri.jmrit.simpleclock.configurexml;

import jmri.InstanceManager;
import jmri.Timebase;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SimpleTimebaseXmlTest {

    @Test
    public void testCTor() {
        SimpleTimebaseXml t = new SimpleTimebaseXml();
        Assert.assertNotNull("exists",t);
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

    @Test
    public void testUpgrade() {
        // Checks what happens when we don't have the new attributes in the XML that we are loading.
        Element e = new Element("timebase");
        // Example XML:
        // <timebase class="jmri.jmrit.simpleclock.configurexml.SimpleTimebaseXml" time="Fri Dec
        // 07 10:50:00 CET 2018" rate="13.0" run="no" master="no" mastername="OpenLCB Clock
        // Consumer for Default Fast Clock" sync="no" correct="no" display="no"
        // startstopped="yes" startsettime="no" startclockoption="2" showbutton="yes" />
        e.setAttribute("rate", "13.0");
        e.setAttribute("run", "no");
        e.setAttribute("master", "no");
        e.setAttribute("mastername", "OpenLCB Clock Consumer for Default Fast Clock");
        e.setAttribute("sync", "no");
        e.setAttribute("correct", "no");
        e.setAttribute("display", "no");
        e.setAttribute("startstopped", "yes");
        e.setAttribute("startsettime", "no");
        e.setAttribute("startclockoption", "0");
        e.setAttribute("showbutton", "yes");

        SimpleTimebaseXml t = new SimpleTimebaseXml();
        t.load(e, new Element("foo"));
        Timebase tb = InstanceManager.getDefault(Timebase.class);

        // Checks that the new properties have values equivalent of legacy behavior.
        assertEquals(Timebase.ClockInitialRunState.DO_STOP, tb.getClockInitialRunState());
        assertEquals(13.0d, tb.getStartRate(), 0.01);
        assertEquals(true, tb.getSetRateAtStart());
    }

    // private final static Logger log = LoggerFactory.getLogger(SimpleTimebaseXmlTest.class);
}
