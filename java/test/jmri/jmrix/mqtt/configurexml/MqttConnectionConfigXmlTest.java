package jmri.jmrix.mqtt.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for MqttConnectionConfigXml class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.5
 */
public class MqttConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXmlTestBase {

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        xmlAdapter = new MqttConnectionConfigXml();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
    }
}
