package jmri.web.servlet.panel;

import jmri.InstanceManager;
import jmri.configurexml.ConfigXmlManager;
import jmri.jmrit.display.IndicatorTrackIcon;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the jmri.web.servlet.panel.ControlPanelServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 * @author Egbert Broerse Copyright (C) 2020
 */
public class ControlPanelServletTest {

    @Test
    public void testCtor() {
        ControlPanelServlet a = new ControlPanelServlet();
        Assert.assertNotNull(a);
    }

    @Test
    public void testIndicatorTrackIconElement() {
        String systemName = "OB1";
        String userName = "Internal OBlock 1";
        ControlPanelServlet servlet = new NullControlPanelServlet();
        OBlock ob = InstanceManager.getDefault(OBlockManager.class).provide(systemName);
        ob.setUserName(userName);
        IndicatorTrackIcon iti = new IndicatorTrackIcon(null);
        iti.setOccBlock(ob.getUserName());
        Element e = ConfigXmlManager.elementFromObject(iti);
        assertEquals(userName, e.getChild("occupancyblock").getValue());
        e = servlet.positionableElement(iti);
        assertEquals(userName, e.getChild("occupancyblock").getValue());
        //System.out.println(e.getChild("oblocksysname").toString()); // servlet is not adding anything to e
        //assertEquals(systemName, e.getChild("oblocksysname").getValue()); / Child (element) not found, NPE
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private static class NullControlPanelServlet extends ControlPanelServlet {

        @Override
        protected String getPanelType() {
            return "ControlPanel";
        }

        @Override
        protected String getJsonPanel(String name) {
            return null;
        }

        @Override
        protected String getXmlPanel(String name) {
            return null;
        }

    }

}
