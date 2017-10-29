package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

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
        JUnitUtil.tearDown();
        cm = null;
    }
}
