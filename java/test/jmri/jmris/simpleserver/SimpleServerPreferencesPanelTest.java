package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the jmri.jmris.simpleserver.SimpleServerPreferencesPanel class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SimpleServerPreferencesPanelTest {

    @Test public void testCtor() {
        SimpleServerPreferencesPanel a = new SimpleServerPreferencesPanel();
        Assert.assertNotNull(a);
    }

    @Before public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
