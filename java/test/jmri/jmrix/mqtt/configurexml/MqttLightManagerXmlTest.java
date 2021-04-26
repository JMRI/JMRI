package jmri.jmrix.mqtt.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for MqttLightManagerXml class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.20.1
 */
public class MqttLightManagerXmlTest {

    @Test
    public void ConstructorTest() {
        Assert.assertNotNull("ConnectionConfig constructor", new MqttLightManagerXml());
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
