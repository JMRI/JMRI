package jmri.jmrix.anyma;

import jmri.implementation.AbstractLightTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for AnymaDMX_UsbLight class.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class AnymaDMX_UsbLightTest extends AbstractLightTestBase {

    private AnymaDMX_SystemConnectionMemo _memo = null;

    @Test
    public void ConstructorTest() {
        AnymaDMX_UsbLight light = new AnymaDMX_UsbLight(
                "DXL1", "Test Light", _memo);
        Assert.assertNotNull("ConnectionConfig constructor", light);
    }

    public void checkOnMsgSent() {
    }

    public void checkOffMsgSent() {
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        _memo = new AnymaDMX_SystemConnectionMemo();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
