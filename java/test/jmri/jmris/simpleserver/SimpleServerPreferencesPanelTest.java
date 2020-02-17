package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


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

    @BeforeEach public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
