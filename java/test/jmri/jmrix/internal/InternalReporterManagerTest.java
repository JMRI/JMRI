package jmri.jmrix.internal;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

/**
 * Test the InternalReporterManager
 *
 * @author Bob Jacobsen 2003, 2006, 2008
 * @author Mark Underwood 2012
 * @author Paul Bender 2016
 */
public class InternalReporterManagerTest extends jmri.managers.AbstractReporterMgrTestBase {

    @Override
    public String getSystemName(String i) {
        return "IR" + i;
    }
    
    @Override
    protected String getASystemNameWithNoPrefix() {
        return "My Reporter 6";
    }
    
    // No manager-specific system name validation at present
    @Test
    @Override
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}
    
    // No manager-specific system name validation at present
    @Test
    @Override
    public void testMakeSystemNameWithPrefixNotASystemName() {}
    
    // No manager-specific system name validation at present
    @Test
    @Override
    public void testIncorrectGetNextValidAddress() {}
    

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        // create and register the manager object
        l = new InternalReporterManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
