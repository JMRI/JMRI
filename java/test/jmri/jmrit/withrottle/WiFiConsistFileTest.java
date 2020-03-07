package jmri.jmrit.withrottle;

import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.jmrit.consisttool.TestConsistManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of WiFiConsistFile
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class WiFiConsistFileTest {

    @Test
    public void testCtor() {
        WiFiConsistManager man = new WiFiConsistManager();
        WiFiConsistFile panel = new WiFiConsistFile(man);
        Assert.assertNotNull("exists", panel );
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initDebugCommandStation();
        JUnitUtil.initRosterConfigManager();
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
    }
    
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
