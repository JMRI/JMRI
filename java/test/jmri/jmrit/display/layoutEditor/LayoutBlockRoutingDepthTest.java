package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.jmrit.display.EditorManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Check that a change to a route, or its removal, passed along a long line
 * of blocks does not need a nested call for each block it reaches. On a large panel the nested
 * calls ran out of stack when the panel was loaded.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class LayoutBlockRoutingDepthTest {

    private static final int BLOCKS = 60;

    // records how deep the stack is each time a route changes
    private static class DepthRecordingManager extends LayoutBlockManager {

        volatile boolean recording;
        int shallowest = Integer.MAX_VALUE;
        int deepest;

        @Override
        void setLastRoutingChange() {
            if (recording) {
                int depth = Thread.currentThread().getStackTrace().length;
                shallowest = Math.min(shallowest, depth);
                deepest = Math.max(deepest, depth);
            }
            super.setLastRoutingChange();
        }
    }

    @TempDir
    File folder;

    // a line of blocks joined by anchors, with an end bumper at each end
    private File writeLinePanel() throws IOException {
        StringBuilder blocks = new StringBuilder();
        StringBuilder layoutBlocks = new StringBuilder();
        StringBuilder track = new StringBuilder();
        for (int i = 1; i <= BLOCKS; i++) {
            blocks.append("    <block systemName=\"IB").append(i).append("\" length=\"100.0\" curve=\"0\">\n")
                    .append("      <systemName>IB").append(i).append("</systemName>\n")
                    .append("      <userName>Line ").append(i).append("</userName>\n")
                    .append("      <permissive>no</permissive>\n")
                    .append("    </block>\n");
            layoutBlocks.append("    <layoutblock systemName=\"ILB").append(i)
                    .append("\" occupiedsense=\"2\" trackcolor=\"darkGray\" occupiedcolor=\"red\" extracolor=\"white\">\n")
                    .append("      <systemName>ILB").append(i).append("</systemName>\n")
                    .append("      <userName>Line ").append(i).append("</userName>\n")
                    .append("    </layoutblock>\n");
            String from = (i == 1) ? "EB1" : "A" + (i - 1);
            String to = (i == BLOCKS) ? "EB2" : "A" + i;
            track.append("    <tracksegment ident=\"T").append(i).append("\" blockname=\"Line ").append(i)
                    .append("\" connect1name=\"").append(from).append("\" type1=\"POS_POINT\" connect2name=\"")
                    .append(to).append("\" type2=\"POS_POINT\" dashed=\"no\" mainline=\"yes\" hidden=\"no\"")
                    .append(" class=\"jmri.jmrit.display.layoutEditor.configurexml.TrackSegmentXml\" />\n");
        }
        track.append(point("EB1", "END_BUMPER", 0, "T1", null));
        for (int i = 1; i < BLOCKS; i++) {
            track.append(point("A" + i, "ANCHOR", i, "T" + i, "T" + (i + 1)));
        }
        track.append(point("EB2", "END_BUMPER", BLOCKS, "T" + BLOCKS, null));

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<layout-config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:noNamespaceSchemaLocation=\"http://jmri.org/xml/schema/layout-4-19-2.xsd\">\n"
                + "  <jmriversion><major>5</major><minor>17</minor><test>5</test><modifier /></jmriversion>\n"
                + "  <blocks class=\"jmri.configurexml.BlockManagerXml\">\n"
                + "    <defaultspeed>Normal</defaultspeed>\n"
                + blocks
                + "  </blocks>\n"
                + "  <layoutblocks class=\"jmri.jmrit.display.layoutEditor.configurexml.LayoutBlockManagerXml\">\n"
                + layoutBlocks
                + "  </layoutblocks>\n"
                + "  <LayoutEditor class=\"jmri.jmrit.display.layoutEditor.configurexml.LayoutEditorXml\""
                + " name=\"Long Line\" x=\"0\" y=\"0\" windowheight=\"400\" windowwidth=\"800\""
                + " panelheight=\"300\" panelwidth=\"" + (BLOCKS * 20 + 40) + "\" sliders=\"no\" scrollable=\"none\""
                + " editable=\"yes\" positionable=\"yes\" controlling=\"yes\" animating=\"yes\" showhelpbar=\"no\""
                + " drawgrid=\"no\" snaponadd=\"no\" snaponmove=\"no\" antialiasing=\"no\" turnoutcircles=\"no\""
                + " tooltipsnotedit=\"no\" tooltipsinedit=\"no\" mainlinetrackwidth=\"4\" xscale=\"1.0\" yscale=\"1.0\""
                + " sidetrackwidth=\"2\" defaulttrackcolor=\"darkGray\" defaultoccupiedtrackcolor=\"red\""
                + " defaultalternativetrackcolor=\"white\" defaulttextcolor=\"black\" turnoutcirclecolor=\"black\""
                + " turnoutcirclesize=\"4\" turnoutdrawunselectedleg=\"yes\" turnoutbx=\"20.0\" turnoutcx=\"20.0\""
                + " turnoutwid=\"10.0\" xoverlong=\"30.0\" xoverhwid=\"10.0\" xovershort=\"10.0\" autoblkgenerate=\"no\""
                + " redBackground=\"192\" greenBackground=\"192\" blueBackground=\"192\" gridSize=\"10\" gridSize2nd=\"10\""
                + " openDispatcher=\"no\" useDirectTurnoutControl=\"no\">\n"
                + track
                + "  </LayoutEditor>\n"
                + "</layout-config>\n";
        File f = new File(folder, "LongLine.xml");
        Files.write(f.toPath(), xml.getBytes(StandardCharsets.UTF_8));
        return f;
    }

    private static String point(String ident, String type, int position, String connect1, String connect2) {
        return "    <positionablepoint ident=\"" + ident + "\" type=\"" + type + "\" x=\"" + (20 + position * 20)
                + ".0\" y=\"100.0\" connect1name=\"" + connect1 + "\""
                + (connect2 == null ? "" : " connect2name=\"" + connect2 + "\"")
                + " class=\"jmri.jmrit.display.layoutEditor.configurexml.PositionablePointXml\" />\n";
    }

    private DepthRecordingManager loadLine() throws Exception {
        DepthRecordingManager lbm = new DepthRecordingManager();
        InstanceManager.setDefault(LayoutBlockManager.class, lbm);
        InstanceManager.getDefault(ConfigureManager.class).registerConfig(lbm, jmri.Manager.LAYOUTBLOCKS);

        assertTrue(InstanceManager.getDefault(ConfigureManager.class).load(writeLinePanel()), "panel loaded");
        lbm.setStabilisedDelay(200);
        lbm.enableAdvancedRouting(true);
        ThreadingUtil.runOnGUI(() -> lbm.initializeLayoutBlockPaths());
        JUnitUtil.waitFor(() -> lbm.routingStablised(), "routing stabilised");
        ThreadingUtil.runOnLayout(() -> {});
        new LayoutBlockRoutingChecker(lbm).assertTablesCorrect("after loading");
        return lbm;
    }

    @Test
    public void testChangeAlongLongLine() throws Exception {
        DepthRecordingManager lbm = loadLine();
        LayoutBlockRoutingChecker checker = new LayoutBlockRoutingChecker(lbm);

        // a cost change at one end reaches every block along the line
        LayoutBlock end = lbm.getLayoutBlock("Line 1");
        ThreadingUtil.runOnLayout(() -> {
            lbm.recording = true;
            end.setBlockMetric(end.getBlockMetric() + 500);
            lbm.recording = false;
        });
        JUnitUtil.waitFor(() -> lbm.routingStablised(), "routing stabilised after raising a metric");
        ThreadingUtil.runOnLayout(() -> {});
        checker.assertTablesCorrect("after raising the metric of " + end.getDisplayName());

        int growth = lbm.deepest - lbm.shallowest;
        assertTrue(lbm.deepest > 0, "the change was passed on");
        assertTrue(growth < BLOCKS, "stack grew by " + growth + " frames passing a change along "
                + BLOCKS + " blocks; it should not grow with the number of blocks");
    }

    @Test
    public void testRemovalAlongLongLine() throws Exception {
        DepthRecordingManager lbm = loadLine();

        // taking away the connection at one end withdraws the routes to the
        // last block from every block along the line
        LayoutBlock last = lbm.getLayoutBlock("Line " + BLOCKS);
        LayoutBlock before = lbm.getLayoutBlock("Line " + (BLOCKS - 1));
        ThreadingUtil.runOnLayout(() -> {
            lbm.recording = true;
            before.notifiedNeighbourNoLongerMutual(last);
            last.notifiedNeighbourNoLongerMutual(before);
            lbm.recording = false;
        });
        JUnitUtil.waitFor(() -> lbm.routingStablised(), "routing stabilised after removing a connection");
        ThreadingUtil.runOnLayout(() -> {});
        for (int i = 1; i < BLOCKS; i++) {
            LayoutBlock lb = lbm.getLayoutBlock("Line " + i);
            for (int r = 0; r < lb.getNumberOfRoutes(); r++) {
                assertTrue(lb.getRouteDestBlockAtIndex(r) != last.getBlock(),
                        "route to " + last.getDisplayName() + " withdrawn at " + lb.getDisplayName());
            }
        }

        int growth = lbm.deepest - lbm.shallowest;
        assertTrue(lbm.deepest > 0, "the removal was passed on");
        assertTrue(growth < BLOCKS, "stack grew by " + growth + " frames passing a removal along "
                + BLOCKS + " blocks; it should not grow with the number of blocks");
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
