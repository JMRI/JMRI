package jmri.jmrit.throttle;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of AddressPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class AddressPanelTest {

    @Test
    public void testCtor() {
        AddressPanel panel = new AddressPanel();
        Assert.assertNotNull("exists", panel);
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
