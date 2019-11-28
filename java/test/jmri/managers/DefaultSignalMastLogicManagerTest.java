package jmri.managers;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DefaultSignalMastLogicManagerTest extends AbstractManagerTestBase<jmri.SignalMastLogicManager,jmri.SignalMastLogic> {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",l);
    }

    @Test
    @Override
    @Ignore("makeSystemName is not currently supported")
    public void testMakeSystemName() {
    }

    @Ignore("This managers doesn't support auto system names")
    @Test
    @Override
    public void testAutoSystemNames() {
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        l = new DefaultSignalMastLogicManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @After
    public void tearDown() {
        l = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastLogicManagerTest.class);

}
