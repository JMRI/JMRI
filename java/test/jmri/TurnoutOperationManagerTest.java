package jmri;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TurnoutOperationManagerTest {

    @Test
    public void testCTor() {
        TurnoutOperationManager t = new TurnoutOperationManager();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testDefaultObjectCreation() {
        TurnoutOperationManager t = jmri.InstanceManager.getDefault(TurnoutOperationManager.class);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutOperationManagerTest.class);

}
