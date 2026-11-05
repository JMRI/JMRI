package jmri.jmrit.logixng.util;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test LogixNG_SelectNamedBean
 */
public class LogixNG_SelectNamedBeanTest {

    private LogixNG_SelectNamedBean<Sensor> selectNamedBean;

    @Test
    public void testDelayedNameNotLookedUpAgainAfterRename() {
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        sensor.setUserName("Old name");
        selectNamedBean.setDelayedNamedBean("Old name");
        selectNamedBean.setup();
        assertSame( sensor, selectNamedBean.getBean(), "the sensor is found at the first setup");

        // renamed in the sensor table
        sensor.setUserName("New name");
        InstanceManager.getDefault(NamedBeanHandleManager.class).renameBean("Old name", "New name", sensor);

        // setup is done again when another file is loaded
        selectNamedBean.setup();
        assertSame( sensor, selectNamedBean.getBean(), "the sensor is kept at the next setup");
    }

    @Test
    public void testDelayedNameLookedUpUntilFound() {
        selectNamedBean.setDelayedNamedBean("Later sensor");
        selectNamedBean.setup();
        assertNull( selectNamedBean.getBean(), "the sensor does not exist yet");
        JUnitAppender.assertErrorMessage("Sensor \"Later sensor\" is not found");

        // created by a file loaded later
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        sensor.setUserName("Later sensor");

        selectNamedBean.setup();
        assertSame( sensor, selectNamedBean.getBean(), "the sensor is found at the next setup");
    }

    @Test
    public void testSettingBeanReplacesDelayedName() {
        Sensor delayed = InstanceManager.getDefault(SensorManager.class).provide("IS3");
        delayed.setUserName("Delayed sensor");
        Sensor chosen = InstanceManager.getDefault(SensorManager.class).provide("IS4");
        selectNamedBean.setDelayedNamedBean("Delayed sensor");

        // chosen in the editor before setup is done
        selectNamedBean.setNamedBean(chosen);
        selectNamedBean.setup();
        assertSame( chosen, selectNamedBean.getBean(), "the chosen sensor is kept");
    }

    @Test
    public void testRemovingBeanReplacesDelayedName() {
        Sensor delayed = InstanceManager.getDefault(SensorManager.class).provide("IS5");
        delayed.setUserName("Delayed sensor");
        selectNamedBean.setDelayedNamedBean("Delayed sensor");

        // removed in the editor before setup is done
        selectNamedBean.removeNamedBean();
        selectNamedBean.setup();
        assertNull( selectNamedBean.getBean(), "no sensor is set");
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
