package jmri.jmrix.mqtt;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for MqttLightManager class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.11.5
 */
public class MqttLightManagerTest {

    @Test
    public void ConstructorTest() {
        MqttSystemConnectionMemo memo = new MqttSystemConnectionMemo();
        Assert.assertNotNull("constructor", new MqttLightManager(memo));
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
