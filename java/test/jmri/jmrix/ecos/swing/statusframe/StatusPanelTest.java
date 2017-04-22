package jmri.jmrix.ecos.swing.statusframe;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of StatusPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class StatusPanelTest {

    jmri.jmrix.ecos.EcosSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        StatusPanel action = new StatusPanel();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        memo = new jmri.jmrix.ecos.EcosSystemConnectionMemo();

        jmri.InstanceManager.store(memo, jmri.jmrix.ecos.EcosSystemConnectionMemo.class);
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
