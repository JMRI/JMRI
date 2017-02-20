package jmri.jmrix.ecos;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import java.awt.GraphicsEnvironment;

/**
 * Tests for the Bundle class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class EcosPreferencesTest  {

    EcosSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        // the constructor builds a PreferencesPane, which is a GUI object.
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", new jmri.jmrix.ecos.EcosPreferences(memo));
    }

    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new jmri.jmrix.ecos.EcosSystemConnectionMemo();

        jmri.InstanceManager.store(memo, jmri.jmrix.ecos.EcosSystemConnectionMemo.class);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
