package jmri.jmrit.withrottle;

import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.consisttool.TestConsistManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of ConsistController
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ConsistControllerTest {

    @Test
    public void testCtor() {
        ConsistController panel = new ConsistController();
        Assert.assertNotNull("exists", panel );
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugCommandStation();
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
        InstanceManager.setDefault(NamedBeanHandleManager.class, new NamedBeanHandleManager());
    }
    
    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
