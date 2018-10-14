package jmri.jmrix.mqtt;

import jmri.util.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for MqttTurnout class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.5
 */
public class MqttTurnoutTest {

    @Test
    public void ConstructorTest() {
        MqttAdapter a = new MqttAdapter();
        Assert.assertNotNull("constructor", new MqttTurnout(a, 2));
        JUnitAppender.assertWarnMessage("Trying to subscribe before connect/configure is done");
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
