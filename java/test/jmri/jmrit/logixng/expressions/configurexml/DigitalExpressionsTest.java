package jmri.jmrit.logixng.expressions.configurexml;

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
 * Test DigitalExpressions
 *
 * @author Daniel Bergqvist 2019
 */
public class DigitalExpressionsTest {

    @Test
    public void testLoad() throws JmriConfigureXmlException {
        AbstractNamedBeanManagerConfigXML b;

        b = new AndXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new AntecedentXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new ExpressionLightXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new ExpressionSensorXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new ExpressionTurnoutXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new FalseXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new HoldXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new OrXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new TriggerOnceXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");

        b = new TrueXml();
        assertNotNull( b, "exists");
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");
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
