package jmri.jmrit.dispatcher;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AutoAllocateTest {

    @Test
    public void testCTor() {
//        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");  // exist?

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        AutoAllocate t = new AutoAllocate(d, new ArrayList<>());
        Assert.assertNotNull("exists",t);
        jmri.util.JUnitAppender.assertErrorMessage("null LayoutEditor when constructing AutoAllocate");
        JUnitUtil.dispose(d);
    }

    @Test
    public void testErrorCase() {
        // present so there's some class test coverage when skipping intermittent
        new AutoAllocate(null,null);
        JUnitAppender.assertErrorMessage("null DispatcherFrame when constructing AutoAllocate");
        
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();  // only needed intermittently; better to find and remove, but that would require lots o' refactoring
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AutoAllocateTest.class);

}
