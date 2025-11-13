package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.SignalGroup;
import jmri.SignalGroupManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultSignalGroupManagerTest extends AbstractManagerTestBase<SignalGroupManager,SignalGroup> {

    @Test
    public void testCTor() {
        assertNotNull( l, "exists");
    }

    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}
    
    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithPrefixNotASystemName() {}
    
    @Test
    public void testProvideByUserName(){
        SignalGroup sg = l.newSignalGroupWithUserName("Sig Group UserName");
        assertNotNull(sg);
        assertEquals( sg.getUserName(),
            l.provideSignalGroup("", "Sig Group UserName").getUserName(),
            "username returned ok");
        assertEquals( "IG:AUTO:0001", sg.getSystemName(), "systemname created ok");
        assertEquals( sg, l.provideSignalGroup("IG:AUTO:0001", null),
            "systemname returned ok");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        l = new DefaultSignalGroupManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @AfterEach
    public void tearDown() {
        l = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DefaultSignalGroupManagerTest.class);

}
