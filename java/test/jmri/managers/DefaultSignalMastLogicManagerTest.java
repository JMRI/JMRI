package jmri.managers;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultSignalMastLogicManagerTest extends AbstractManagerTestBase<jmri.SignalMastLogicManager,jmri.SignalMastLogic> {

    @Test
    public void testCTor() {
        Assertions.assertNotNull( l, "exists" );
    }

    @Test
    @Override
    @Disabled("makeSystemName is not currently supported")
    public void testMakeSystemName() {
    }

    @Disabled("This managers doesn't support auto system names")
    @Test
    @Override
    public void testAutoSystemNames() {
    }
    
    @Disabled("This manager doesn't support auto system names")
    @Test
    @Override
    public void testMakeSystemNameWithPrefix() {
    }
    
    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}
    
    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithPrefixNotASystemName() {}

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        l = new DefaultSignalMastLogicManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @AfterEach
    public void tearDown() {
        l.dispose();
        l = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastLogicManagerTest.class);

}
