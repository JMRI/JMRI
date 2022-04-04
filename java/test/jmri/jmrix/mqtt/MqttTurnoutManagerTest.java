package jmri.jmrix.mqtt;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for MqttTurnoutManager class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.5
 */
public class MqttTurnoutManagerTest {

    @Test
    public void ConstructorTest() {
        new MqttAdapter();
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
