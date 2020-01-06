package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.*;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.swing.*;
import jmri.InstanceManager;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.*;
import jmri.util.junit.rules.RetryRule;
import org.junit.*;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.*;

/**
 * @author Paul Bender Copyright (C) 2017
 * @author George Warner Copyright: (C) 2019
 */
public class LayoutEditorChecksTest {

    @Rule   //10 second timeout for methods in this test class.
    public Timeout globalTimeout = Timeout.seconds(10);

    @Rule   //allow 3 retries
    public RetryRule retryRule = new RetryRule(3);

    private static Operator.StringComparator stringComparator = null;

    //LayoutEditorChecks Bundle Strings
    private String toolsMenuTitle = Bundle.getMessage("MenuTools");
    private String checkMenuTitle = Bundle.getMessage("CheckMenuTitle");
    private String checkUnConnectedTracksMenuTitle = Bundle.getMessage("CheckUnConnectedTracksMenuTitle");
    private String checkUnBlockedTracksMenuTitle = Bundle.getMessage("CheckUnBlockedTracksMenuTitle");
    private String checkNonContiguousBlocksMenuTitle = Bundle.getMessage("CheckNonContiguousBlocksMenuTitle");
    private String checkUnnecessaryAnchorsMenuTitle = Bundle.getMessage("CheckUnnecessaryAnchorsMenuTitle");

    //name strings
    private static String myBlockName = "My Block";
    private static String rightHandName = "Right Hand";
    private static String leftHandName = "Left Hand";

    //jemmy operators
    private static EditorFrameOperator layoutEditorEFO = null;

    private static LayoutEditor layoutEditor = null;
    private static LayoutEditorChecks layoutEditorChecks = null;
    private static LayoutBlock layoutBlock = null;
    private static LayoutTurnout ltRH = null, ltLH = null;
    private static PositionablePoint a1 = null;
    private static PositionablePoint a2 = null;
    private static TrackSegment ts1 = null;
    private static TrackSegment ts2 = null;
    private static TrackSegment ts3 = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditorChecks null", layoutEditorChecks);
    }

    @Test
    public void testToolsCheckMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("toolsMenuTitle null", toolsMenuTitle);
        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        Assert.assertNotNull("CheckMenuTitle null", checkMenuTitle);
        toolsJMO.pushMenu(toolsMenuTitle + "/" + checkMenuTitle, "/");
    }

    @Test
    public void testCheckUnConnectedTracks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("toolsMenuTitle null", toolsMenuTitle);
        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        Assert.assertNotNull("CheckMenuTitle null", checkMenuTitle);
        Assert.assertNotNull("checkUnConnectedTracksMenuTitle null", checkUnConnectedTracksMenuTitle);

        JPopupMenu toolsPopupMenu = toolsJMO.getPopupMenu();
        JMenuItem checkMenuItem = (JMenuItem) toolsPopupMenu.getComponent(0);
        Assert.assertEquals("checkMenuItem.getText(): ",
                checkMenuTitle, checkMenuItem.getText());

        JMenuOperator checkJMO = new JMenuOperator((JMenu) checkMenuItem);
        JPopupMenu checksPopupMenu = checkJMO.getPopupMenu();
        JMenuItem unConnectedTracksMenuItem = (JMenuItem) checksPopupMenu.getComponent(0);
        Assert.assertEquals("subMenuItem.getText(): ",
                checkUnConnectedTracksMenuTitle, unConnectedTracksMenuItem.getText());

        JMenuOperator unConnectedTracksJMO = new JMenuOperator((JMenu) unConnectedTracksMenuItem);
        unConnectedTracksJMO.doClick();

        JPopupMenu unConnectedTracksPopupMenu = unConnectedTracksJMO.getPopupMenu();

        //verify results
        Assert.assertEquals("Correct number of check unconnected tracks results menu items",
                2, unConnectedTracksPopupMenu.getSubElements().length);
        JMenuItem resultsMenuItem = (JMenuItem) unConnectedTracksPopupMenu.getComponent(0);
        Assert.assertEquals("resultsMenuItem.getText(): ", rightHandName, resultsMenuItem.getText());

        resultsMenuItem = (JMenuItem) unConnectedTracksPopupMenu.getComponent(1);
        Assert.assertEquals("resultsMenuItem.getText(): ", leftHandName, resultsMenuItem.getText());
    }   //testCheckUnConnectedTracks

    @Test
    public void testCheckUnBlockedTracks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("toolsMenuTitle null", toolsMenuTitle);
        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        Assert.assertNotNull("CheckMenuTitle null", checkMenuTitle);
        Assert.assertNotNull("checkUnBlockedTracksMenuTitle null", checkUnBlockedTracksMenuTitle);

        JPopupMenu toolsPopupMenu = toolsJMO.getPopupMenu();
        JMenuItem checkMenuItem = (JMenuItem) toolsPopupMenu.getComponent(0);
        Assert.assertEquals("checkMenuItem.getText(): ",
                checkMenuTitle, checkMenuItem.getText());

        JMenuOperator checkJMO = new JMenuOperator((JMenu) checkMenuItem);
        JPopupMenu checksPopupMenu = checkJMO.getPopupMenu();
        JMenuItem checkUnBlockedTracksMenuItem = (JMenuItem) checksPopupMenu.getComponent(1);
        Assert.assertEquals("subMenuItem.getText(): ",
                checkUnBlockedTracksMenuTitle, checkUnBlockedTracksMenuItem.getText());

        JMenuOperator checkUnBlockedTracksJMO = new JMenuOperator((JMenu) checkUnBlockedTracksMenuItem);
        checkUnBlockedTracksJMO.doClick();

        JPopupMenu checkUnBlockedTracksPopupMenu = checkUnBlockedTracksJMO.getPopupMenu();

        //verify results
        Assert.assertEquals("Correct number of check unblocked tracks results menu items",
                3, checkUnBlockedTracksPopupMenu.getSubElements().length);
        JMenuItem resultsMenuItem = (JMenuItem) checkUnBlockedTracksPopupMenu.getComponent(0);
        Assert.assertEquals("resultsMenuItem0.getText(): ", ts1.getName(), resultsMenuItem.getText());
        resultsMenuItem = (JMenuItem) checkUnBlockedTracksPopupMenu.getComponent(1);
        Assert.assertEquals("resultsMenuItem1.getText(): ", ts2.getName(), resultsMenuItem.getText());
        resultsMenuItem = (JMenuItem) checkUnBlockedTracksPopupMenu.getComponent(2);
        Assert.assertEquals("resultsMenuItem2.getText(): ", ts3.getName(), resultsMenuItem.getText());
    }   //testCheckUnBlockedTracks

    @Test
    public void testCheckNonContiguousBlocks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("toolsMenuTitle null", toolsMenuTitle);
        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        Assert.assertNotNull("CheckMenuTitle null", checkMenuTitle);
        Assert.assertNotNull("checkNonContiguousBlocksMenuTitle null", checkNonContiguousBlocksMenuTitle);

        JPopupMenu toolsPopupMenu = toolsJMO.getPopupMenu();
        JMenuItem checkMenuItem = (JMenuItem) toolsPopupMenu.getComponent(0);
        Assert.assertEquals("checkMenuItem.getText(): ",
                checkMenuTitle, checkMenuItem.getText());

        JMenuOperator checkJMO = new JMenuOperator((JMenu) checkMenuItem);
        JPopupMenu checksPopupMenu = checkJMO.getPopupMenu();
        JMenuItem checkNonContiguousBlocksMenuItem = (JMenuItem) checksPopupMenu.getComponent(2);
        Assert.assertEquals("subMenuItem.getText(): ",
                checkNonContiguousBlocksMenuTitle, checkNonContiguousBlocksMenuItem.getText());

        JMenuOperator checkNonContiguousBlocksJMO = new JMenuOperator((JMenu) checkNonContiguousBlocksMenuItem);
        checkNonContiguousBlocksJMO.doClick();

        JPopupMenu checkNonContiguousBlocksPopupMenu = checkNonContiguousBlocksJMO.getPopupMenu();

        //verify results
        Assert.assertEquals("Correct number of check noncontiguous blocks results menu items",
                1, checkNonContiguousBlocksPopupMenu.getSubElements().length);
        JMenuItem resultsMenuItem = (JMenuItem) checkNonContiguousBlocksPopupMenu.getComponent(0);
        Assert.assertEquals("resultsMenuItem0.getText(): ", myBlockName, resultsMenuItem.getText());

        JMenuOperator resultsJMO = new JMenuOperator((JMenu) resultsMenuItem);
        JPopupMenu resultsPopupMenu = resultsJMO.getPopupMenu();
        Assert.assertEquals("Correct number of check noncontiguous blocks results submenu items",
                2, resultsPopupMenu.getSubElements().length);
        JMenuItem resultsSubMenuItem = (JMenuItem) resultsPopupMenu.getComponent(0);
        Assert.assertEquals("resultsSubMenuItem0.getText(): ", myBlockName + ":#1", resultsSubMenuItem.getText());
        resultsSubMenuItem = (JMenuItem) resultsPopupMenu.getComponent(1);
        Assert.assertEquals("resultsSubMenuItem1.getText(): ", myBlockName + ":#2", resultsSubMenuItem.getText());
    }   //testCheckNonContiguousBlocks

    @Test
    public void testCheckUnnecessaryAnchors() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("toolsMenuTitle null", toolsMenuTitle);
        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        Assert.assertNotNull("CheckMenuTitle null", checkMenuTitle);
        Assert.assertNotNull("checkUnnecessaryAnchorsMenuTitle null", checkUnnecessaryAnchorsMenuTitle);

        JPopupMenu toolsPopupMenu = toolsJMO.getPopupMenu();
        JMenuItem checkMenuItem = (JMenuItem) toolsPopupMenu.getComponent(0);
        Assert.assertEquals("checkMenuItem.getText(): ",
                checkMenuTitle, checkMenuItem.getText());

        JMenuOperator checkJMO = new JMenuOperator((JMenu) checkMenuItem);
        JPopupMenu checksPopupMenu = checkJMO.getPopupMenu();
        JMenuItem checkUnnecessaryAnchorsMenuItem = (JMenuItem) checksPopupMenu.getComponent(3);
        Assert.assertEquals("subMenuItem.getText(): ",
                checkUnnecessaryAnchorsMenuTitle, checkUnnecessaryAnchorsMenuItem.getText());

        JMenuOperator checkUnnecessaryAnchorsJMO = new JMenuOperator((JMenu) checkUnnecessaryAnchorsMenuItem);
        checkUnnecessaryAnchorsJMO.doClick();

        JPopupMenu checkUnnecessaryAnchorsPopupMenu = checkUnnecessaryAnchorsJMO.getPopupMenu();

        //verify results
        Assert.assertEquals("Correct number of check unnecessary anchors results menu items",
                2, checkUnnecessaryAnchorsPopupMenu.getSubElements().length);
        JMenuItem resultsMenuItem = (JMenuItem) checkUnnecessaryAnchorsPopupMenu.getComponent(0);
        Assert.assertEquals("resultsMenuItem0.getText(): ", a1.getName(), resultsMenuItem.getText());
        resultsMenuItem = (JMenuItem) checkUnnecessaryAnchorsPopupMenu.getComponent(1);
        Assert.assertEquals("resultsMenuItem1.getText(): ", a2.getName(), resultsMenuItem.getText());
    }   //testCheckUnnecessaryAnchors

    @BeforeClass
    public static void setUpClass() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();

            //save the old string comparator
            stringComparator = Operator.getDefaultStringComparator();
            //set default string matching comparator to one that exactly matches and is case sensitive
            Operator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));

            layoutEditor = new LayoutEditor("Layout Editor Checks Test Layout");
            layoutEditor.setPanelBounds(new Rectangle2D.Double(0, 0, 640, 480));
            layoutEditor.setVisible(true);
            layoutEditorChecks = layoutEditor.getLEChecks();

            layoutEditorEFO = new EditorFrameOperator(layoutEditor);

            Point2D point = new Point2D.Double(150.0, 100.0);
            Point2D delta = new Point2D.Double(100.0, 50.0);
            List<LayoutTrack> layoutTracks = layoutEditor.getLayoutTracks();
            Assert.assertNotNull("layoutTracks null", layoutTracks);

            //create a layout block
            layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, myBlockName);

            //add 1st turnout
            ltRH = new LayoutTurnout(rightHandName, LayoutTurnout.RH_TURNOUT,
                    point, 33.0, 1.1, 1.2, layoutEditor);
            layoutTracks.add(ltRH);
            //set its block
            ltRH.setLayoutBlock(layoutBlock);

            //add 1st anchor
            point = MathUtil.add(point, delta);
            a1 = new PositionablePoint("A1", PositionablePoint.ANCHOR, point, layoutEditor);
            Assert.assertNotNull("PositionablePoint", a1);
            layoutTracks.add(a1);

            //connect 1st turnout to 1st anchor
            int tsIdx = 1;
            ts1 = addNewTrackSegment(ltRH, LayoutTrack.TURNOUT_B, a1, LayoutTrack.POS_POINT, tsIdx++);

            //add 2nd anchor
            //point = MathUtil.add(point, delta);
            a2 = new PositionablePoint("A2", PositionablePoint.ANCHOR, point, layoutEditor);
            Assert.assertNotNull("PositionablePoint", a2);
            layoutTracks.add(a2);

            //connect 1st anchor to 2nd anchor
            ts2 = addNewTrackSegment(a1, LayoutTrack.POS_POINT, a2, LayoutTrack.POS_POINT, tsIdx++);
            //set its block
            //ts2.setLayoutBlock(layoutBlock);

            //add 2nd turnout
            point = MathUtil.add(point, delta);
            ltLH = new LayoutTurnout(leftHandName, LayoutTurnout.LH_TURNOUT,
                    point, 66.0, 1.3, 1.4, layoutEditor);
            layoutTracks.add(ltLH);
            //set its block
            ltLH.setLayoutBlock(layoutBlock);

            //connect 2nd anchor to 2nd turnout
            ts3 = addNewTrackSegment(a2, LayoutTrack.POS_POINT, ltLH, LayoutTrack.TURNOUT_A, tsIdx++);

            //wait for layout editor to finish setup and drawing
            new QueueTool().waitEmpty();
        }
    }

    private static TrackSegment addNewTrackSegment(
            @CheckForNull LayoutTrack c1, int t1,
            @CheckForNull LayoutTrack c2, int t2,
            int idx) {
        TrackSegment result = null;
        if ((c1 != null) && (c2 != null)) {
            //create new track segment
            String name = layoutEditor.getFinder().uniqueName("T", idx);
            result = new TrackSegment(name, c1, t1, c2, t2,
                    false, false, layoutEditor);
            layoutEditor.getLayoutTracks().add(result);
            //link to connected objects
            layoutEditor.setLink(c1, t1, result, LayoutTrack.TRACK);
            layoutEditor.setLink(c2, t2, result, LayoutTrack.TRACK);
        }
        return result;
    }

    @AfterClass
    public static void tearDownClass() {
        if (!GraphicsEnvironment.isHeadless()) {
            new QueueTool().waitEmpty();
            JUnitUtil.dispose(layoutEditor);
            layoutEditor = null;
            layoutEditorChecks = null;
            layoutEditorEFO.requestClose();
            layoutEditorEFO.waitClosed();
            //restore the default string matching comparator
            Operator.setDefaultStringComparator(stringComparator);
        }
        JUnitUtil.tearDown();
    }
}
