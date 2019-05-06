package jmri.managers;

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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        l = new DefaultSignalMastLogicManager();
    }

    @After
    public void tearDown() {
        l = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastLogicManagerTest.class);

}
