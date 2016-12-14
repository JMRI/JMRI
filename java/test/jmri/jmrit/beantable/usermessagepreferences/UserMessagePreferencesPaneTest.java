package jmri.jmrit.beantable.usermessagepreferences;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of UserMessagePreferencesPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class UserMessagePreferencesPaneTest {

    @Test
    public void testCtor() {
        UserMessagePreferencesPane p = new UserMessagePreferencesPane(); 
        Assert.assertNotNull("exists", p);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
