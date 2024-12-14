package jmri.managers;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.SignalGroup;
import jmri.SignalGroupManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DefaultSignalGroupManagerTest extends AbstractManagerTestBase<SignalGroupManager,SignalGroup> {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",l);
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
        Assert.assertNotNull(sg);
        Assert.assertEquals("username returned ok",sg.getUserName(), l.provideSignalGroup("", "Sig Group UserName").getUserName());
        Assert.assertEquals("systemname created ok","IG:AUTO:0001",sg.getSystemName());
        Assert.assertEquals("systemname returned ok",sg,l.provideSignalGroup("IG:AUTO:0001", null));
    
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
