package jmri.jmrit.beantable.usermessagepreferences;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Test simple functioning of UserMessagePreferencesPane
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class UserMessagePreferencesPaneTest {

    @Test
    public void testCtor() {
        UserMessagePreferencesPane p = new UserMessagePreferencesPane(); 
        Assert.assertNotNull("exists", p);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
