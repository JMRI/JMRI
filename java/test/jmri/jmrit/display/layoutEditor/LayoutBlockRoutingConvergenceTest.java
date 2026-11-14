package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.InstanceManager;
import jmri.jmrit.display.EditorManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Load sample panels with block routing on and check every route held by
 * every LayoutBlock against a separate shortest path calculation over the
 * same blocks, neighbours and through paths, both after loading and after a
 * block metric has been changed and changed back.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class LayoutBlockRoutingConvergenceTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "GeoPanelsAnon.xml",
        "DS-Sample-Layout-4-19-6.xml",
    })
    public void testRoutingTablesConverge(String panel) throws Exception {
        LayoutBlockManager lbm = LayoutBlockRoutingChecker.loadPanel(panel);
        LayoutBlockRoutingChecker checker = new LayoutBlockRoutingChecker(lbm);
        checker.assertTablesCorrect("after loading");

        // raise the metric of the block that most routes go through
        LayoutBlock changed = checker.busiestBlock();
        int original = changed.getBlockMetric();
        ThreadingUtil.runOnLayout(() -> changed.setBlockMetric(original + 1000));
        JUnitUtil.waitFor(() -> lbm.routingStablised(), "routing stabilised after raising a metric");
        assertEquals(original + 1000, changed.getBlockMetric());
        checker.assertTablesCorrect("after raising the metric of " + changed.getDisplayName());

        ThreadingUtil.runOnLayout(() -> changed.setBlockMetric(original));
        JUnitUtil.waitFor(() -> lbm.routingStablised(), "routing stabilised after restoring a metric");
        checker.assertTablesCorrect("after restoring the metric of " + changed.getDisplayName());
    }
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initMemoryManager();
    }

    @AfterEach
    public void tearDown() {
        for (LayoutEditor panel : InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class)) {
            JUnitUtil.dispose(panel);
        }
        JUnitUtil.clearRouteThreads();
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.removeMatchingThreads("Routing stabilising timer");
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
