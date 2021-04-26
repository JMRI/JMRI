package jmri.jmrix.mqtt;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the MQTT sensor class.
 *
 * @author Bob Jacobsen Copyright 2002, 2020
 */
public class MqttSensorManagerTest {

    @Test
    public void ConstructorTest() {
        Assert.assertNotNull("constructor", new MqttSensorManager(new MqttSystemConnectionMemo()));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
