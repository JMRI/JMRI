package jmri.jmrit.withrottle;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.util.JUnitUtil;

/**
 * Test simple functioning of WiFiConsistManager
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class WiFiConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        cm = new WiFiConsistManager();
    }
    
    @After
    @Override
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
        cm = null;
    }
}
