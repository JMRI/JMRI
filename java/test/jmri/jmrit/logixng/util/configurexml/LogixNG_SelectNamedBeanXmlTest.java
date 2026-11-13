package jmri.jmrit.logixng.util.configurexml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test LogixNG_SelectNamedBeanXml
 */
public class LogixNG_SelectNamedBeanXmlTest {

    private LogixNG_SelectNamedBean<Sensor> selectNamedBean;

    private Element namedBeanElement(String name) {
        Element element = new Element("namedBean");
        element.addContent(new Element("addressing").addContent(NamedBeanAddressing.Direct.name()));
        element.addContent(new Element("name").addContent(name));
        return element;
    }

    @Test
    public void testLoadFindsBean() {
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        sensor.setUserName("My sensor");

        assertDoesNotThrow( () -> new LogixNG_SelectNamedBeanXml<Sensor>()
                .load(namedBeanElement("My sensor"), selectNamedBean));
        assertSame( sensor, selectNamedBean.getBean(), "the sensor is found");
    }

    @Test
    public void testLoadMissingBeanLogsError() {
        assertDoesNotThrow( () -> new LogixNG_SelectNamedBeanXml<Sensor>()
                .load(namedBeanElement("Missing sensor"), selectNamedBean));
        assertNull( selectNamedBean.getBean(), "no sensor is set");
        JUnitAppender.assertErrorMessage("Sensor \"Missing sensor\" is not found");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initLogixNGManager();

        selectNamedBean = new ExpressionSensor("IQDE321", null).getSelectNamedBean();
    }

    @AfterEach
    public void tearDown() {
        selectNamedBean = null;
        LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
