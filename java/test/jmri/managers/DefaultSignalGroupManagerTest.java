package jmri.managers;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DefaultSignalGroupManagerTest extends AbstractManagerTestBase<jmri.SignalGroupManager,jmri.SignalGroup> {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",l);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        l = new DefaultSignalGroupManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @After
    public void tearDown() {
        l = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DefaultSignalGroupManagerTest.class);

}
