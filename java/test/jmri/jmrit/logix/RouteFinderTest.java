package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RouteFinderTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NXFrame nxFrame = new NXFrame();
        BlockOrder orig = new BlockOrder(new OBlock("OB1", "Test1"));
        BlockOrder dest = new BlockOrder(new OBlock("OB2", "Test2"));
        BlockOrder via = new BlockOrder(new OBlock("OB3", "Test3"));
        BlockOrder avoid = new BlockOrder(new OBlock("OB4", "Test4"));
        RouteFinder t = new RouteFinder(nxFrame, orig, dest, via, avoid, 3);
        assertThat(t).withFailMessage("exists").isNotNull();
        JUnitUtil.dispose(nxFrame);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        if (InstanceManager.containsDefault(ShutDownManager.class)) {
            List<ShutDownTask> list = new ArrayList<>();
            ShutDownManager sm = InstanceManager.getDefault(jmri.ShutDownManager.class);
            for (Runnable r : sm.getRunnables()) {
                if (r instanceof jmri.jmrit.logix.WarrantShutdownTask) {
                    list.add((ShutDownTask)r);
                }
            }
            for ( ShutDownTask t : list) {
                sm.deregister(t);
            }
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
