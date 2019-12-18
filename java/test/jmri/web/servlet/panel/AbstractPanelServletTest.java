package jmri.web.servlet.panel;

import static org.junit.Assert.assertEquals;

import org.jdom2.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.configurexml.ConfigXmlManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.MultiSensorIcon;
import jmri.util.JUnitUtil;

public class AbstractPanelServletTest {

    @Test
    public void testPositionableElement() {
        String systemName = "IS1";
        String userName = "Internal Sensor 1";
        AbstractPanelServlet servlet = new NullPanelServlet();
        Sensor s = InstanceManager.getDefault(SensorManager.class).provide(systemName);
        s.setUserName(userName);
        MultiSensorIcon p = new MultiSensorIcon(null);
        p.addEntry(userName, new NamedIcon("program:resources/logo.gif", "logo"));
        Element e = ConfigXmlManager.elementFromObject(p);
        assertEquals(userName, e.getChild("active").getAttribute("sensor").getValue());
        e = servlet.positionableElement(p);
        assertEquals(systemName, e.getChild("active").getAttribute("sensor").getValue());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private class NullPanelServlet extends AbstractPanelServlet {

        @Override
        protected String getPanelType() {
            return null;
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