package jmri.jmrix.mqtt;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for MqttConnectionConfig class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.5
 */
public class MqttConnectionConfigTest extends jmri.jmrix.AbstractNetworkConnectionConfigTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        cc = new MqttConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        cc = null;
        JUnitUtil.tearDown();
    }
}
