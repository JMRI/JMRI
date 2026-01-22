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
 * Test ActionTurnoutXml
 *
 * @author Daniel Bergqvist 2019
 */
public class StringExpressionsTest {

    @Test
    public void testLoad() throws JmriConfigureXmlException {
        AbstractNamedBeanManagerConfigXML b;

        b = new StringExpressionMemoryXml();
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
