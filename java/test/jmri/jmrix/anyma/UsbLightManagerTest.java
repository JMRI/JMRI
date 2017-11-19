package jmri.jmrix.anyma;

import jmri.managers.AbstractLightMgrTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for UsbLightManager class.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class UsbLightManagerTest extends AbstractLightMgrTestBase {

    private AnymaDMX_SystemConnectionMemo _memo = null;

    @Test
    public void ConstructorTest() {
        Assert.assertNotNull("ConnectionConfig constructor",
                new UsbLightManager(_memo));
    }

    public String getSystemName(int i) {
        return null;
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
