package jmri.jmrix.mqtt;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for MqttConnectionConfig class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.5
 */
public class MqttConnectionConfigTest extends jmri.jmrix.AbstractNetworkConnectionConfigTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new MqttConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        cc = null;
        JUnitUtil.tearDown();
    }
}
