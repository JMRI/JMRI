package jmri.jmrix.mqtt.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.mqtt.MqttConnectionConfig;


/**
 * Tests for MqttConnectionConfigXml class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.5
 */
public class MqttConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXmlTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        xmlAdapter = new MqttConnectionConfigXml();
        cc = new MqttConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
