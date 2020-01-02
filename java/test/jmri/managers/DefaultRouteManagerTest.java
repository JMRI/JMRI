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
public class DefaultRouteManagerTest extends AbstractProvidingManagerTestBase<jmri.RouteManager,jmri.Route> {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",l);
    }
    
    @Test
    @Override
    @Deprecated // 4.17.7, eventually should check for IR in which case remove this test to allow super implementation
    public void testProvideEmpty() throws IllegalArgumentException {
        l.provide("Foo"); // this should _NOT_ throw an IllegalArgumentException currently
        jmri.util.JUnitAppender.assertWarnMessage("Invalid Route Name: Foo must start with IR");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        l = new DefaultRouteManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @After
    public void tearDown() {
        l = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DefaultRouteManagerTest.class);

}
