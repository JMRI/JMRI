package jmri.jmrit.logixng.actions.configurexml;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.jdom2.Element;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ActionTurnoutXml
 *
 * @author Daniel Bergqvist 2019
 */
public class DigitalActionsXmlTest {

    @Test
    public void testLoad() throws JmriConfigureXmlException {
        AbstractNamedBeanManagerConfigXML b;

        b = new ActionLightXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new ActionSensorXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        // If the ActionScript is in a panel file, it will be replaced with
        // the ActionSimpleScript. That's why the ActionScriptXml class is
        // still there.
        b = new ActionSimpleScriptXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new ActionThrottleXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new ActionTurnoutXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new DoAnalogActionXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new DoStringActionXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new IfThenElseXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new DigitalManyXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new ShutdownComputerXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");
    }

    @Test
    public void testShutdownComputerXml() throws JmriConfigureXmlException {
        Element element = new Element("shutdown-computer");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent("IQDA1"));

        // Test invalid type. This value should be a number but test it with
        // some letters.
//        element.setAttribute("seconds", "abc");

        ShutdownComputerXml shutdownComputerXml = new ShutdownComputerXml();
        shutdownComputerXml.load(element, null);
//        JUnitAppender.assertErrorMessage("seconds attribute is not an integer");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
