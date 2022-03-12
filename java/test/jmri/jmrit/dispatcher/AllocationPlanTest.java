package jmri.jmrit.dispatcher;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AllocationPlanTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");  // exist?
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        AutoAllocate aa = new AutoAllocate(d, new ArrayList<>() );
        jmri.util.JUnitAppender.assertErrorMessage("null LayoutEditor when constructing AutoAllocate");
        AllocationPlan t = new AllocationPlan(aa,1);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(d);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AllocationPlanTest.class);

}
