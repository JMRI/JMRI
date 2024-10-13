package jmri.managers;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AbstractSignalHeadManagerTest extends AbstractManagerTestBase<jmri.SignalHeadManager,jmri.SignalHead> {

    @Test
    public void testCTor() {
        Assertions.assertNotNull( l, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        l = new AbstractSignalHeadManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @AfterEach
    public void tearDown() {
        l = null;
        JUnitUtil.tearDown();
    }
    
    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}
    
    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithPrefixNotASystemName() {}

    // private final static Logger log = LoggerFactory.getLogger(AbstractSignalHeadManagerTest.class);

}
