package jmri.jmrix.anyma;

import jmri.implementation.AbstractLightTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Tests for AnymaDMX_UsbLight class.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class AnymaDMX_UsbLightTest extends AbstractLightTestBase {

    private AnymaDMX_SystemConnectionMemo _memo = null;

    public void testCreate() {
        t = new AnymaDMX_UsbLight("DL1", "Test Light", _memo);
        Assert.assertNotNull("testCreate", t);
    }

    public int numListeners() {
        return 0;
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
        t = new AnymaDMX_UsbLight("DL1", "Test Light", _memo);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
