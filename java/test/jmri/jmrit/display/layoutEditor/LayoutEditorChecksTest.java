package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import jmri.InstanceManager;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.JUnitUtil;
import jmri.util.MathUtil;
import jmri.util.junit.rules.RetryRule;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JMenuOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author George Warner Copyright: (C) 2019
 */
public class LayoutEditorChecksTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(1); // 10 second timeout for methods in this test class.

    @Rule    // allow 2 retries of intermittent tests
    public RetryRule retryRule = new RetryRule(3); // allow 2 retries

    //LayoutEditorChecks Bundle Strings
    private String toolsMenuTitle = Bundle.getMessage("MenuTools");
    private String checkMenuTitle = Bundle.getMessage("CheckMenuTitle");
    private String checkUnConnectedTracksMenuTitle = Bundle.getMessage("CheckUnConnectedTracksMenuTitle");
    private String checkUnBlockedTracksMenuTitle = Bundle.getMessage("CheckUnBlockedTracksMenuTitle");
    private String checkNonContiguousBlocksMenuTitle = Bundle.getMessage("CheckNonContiguousBlocksMenuTitle");
    private String checkUnnecessaryAnchorsMenuTitle = Bundle.getMessage("CheckUnnecessaryAnchorsMenuTitle");

    private static String myBlockName = "My Block";
    private static String rightHandName = "Right Hand";
    private static String leftHandName = "Left Hand";

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
        Assert.assertNotNull("exists", layoutEditorChecks);
    }

    @Test
    public void testSetup() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor not null", layoutEditor);

        Assert.assertNotNull("layoutBlock not null", layoutBlock);
        Assert.assertEquals("layoutBlock.getUserName()", myBlockName, layoutBlock.getUserName());

        Assert.assertNotNull("ltRH not null", ltRH);
        Assert.assertEquals("ltRH.getName()", rightHandName, ltRH.getName());

        Assert.assertNotNull("ltLH not null", ltLH);
        Assert.assertEquals("ltLH.getName()", leftHandName, ltLH.getName());

        Assert.assertNotNull("a1 not null", a1);
        Assert.assertEquals("a1.getName()", "A1", a1.getName());

        Assert.assertNotNull("a2 not null", a2);
        Assert.assertEquals("a2.getName()", "A2", a2.getName());

        Assert.assertNotNull("ts1 not null", ts1);
        Assert.assertEquals("ts1.getName()", "T1", ts1.getName());

        Assert.assertNotNull("ts2 not null", ts2);
        Assert.assertEquals("ts2.getName()", "T2", ts2.getName());

        Assert.assertNotNull("ts3 not null", ts3);
        Assert.assertEquals("ts3.getName()", "T3", ts3.getName());

    }

    @Test
    public void testBundleStrings() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertNotNull("toolsMenuTitle not null", toolsMenuTitle);
        Assert.assertNotNull("CheckMenuTitle not null", checkMenuTitle);

        Assert.assertNotNull("checkUnConnectedTracksMenuTitle not null", checkUnConnectedTracksMenuTitle);
        Assert.assertNotNull("checkUnBlockedTracksMenuTitle not null", checkUnBlockedTracksMenuTitle);
        Assert.assertNotNull("checkNonContiguousBlocksMenuTitle not null", checkNonContiguousBlocksMenuTitle);
        Assert.assertNotNull("checkUnnecessaryAnchorsMenuTitle not null", checkUnnecessaryAnchorsMenuTitle);
    }

    @Test
    public void checkToolsCheckMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("toolsMenuTitle not null", toolsMenuTitle);
        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        Assert.assertNotNull("CheckMenuTitle not null", checkMenuTitle);
        toolsJMO.pushMenuNoBlock(toolsMenuTitle + "/" + checkMenuTitle, "/");
///        Assert.assertEquals("Menu Item Count", 17, checkJMO.getItemCount());
    }

    ///@Test
    public void testFoo() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("toolsMenuTitle not null", toolsMenuTitle);
        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        Assert.assertNotNull("CheckMenuTitle not null", checkMenuTitle);
        Assert.assertNotNull("checkUnConnectedTracksMenuTitle not null", checkUnConnectedTracksMenuTitle);
        String paths[] = {toolsMenuTitle, checkMenuTitle, checkUnConnectedTracksMenuTitle, "TO1"};
        toolsJMO.showMenuItem(paths);
        //dumpScreen();
    }

    @Test
    public void testCheckUnConnectedTracks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("toolsMenuTitle not null", toolsMenuTitle);
        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        Assert.assertNotNull("CheckMenuTitle not null", checkMenuTitle);
        Assert.assertNotNull("checkUnConnectedTracksMenuTitle not null", checkUnConnectedTracksMenuTitle);
        toolsJMO.pushMenuNoBlock(toolsMenuTitle + "/" + checkMenuTitle + "/"
                + checkUnConnectedTracksMenuTitle, "/");
        new QueueTool().waitEmpty();

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
        unConnectedTracksJMO.push();
        JPopupMenu unConnectedTracksPopupMenu = unConnectedTracksJMO.getPopupMenu();

        //verify results
        JMenuItem resultsMenuItem = (JMenuItem) unConnectedTracksPopupMenu.getComponent(0);
        Assert.assertEquals("resultsMenuItem.getText(): ",
                rightHandName, resultsMenuItem.getText());

        resultsMenuItem = (JMenuItem) unConnectedTracksPopupMenu.getComponent(1);
        Assert.assertEquals("resultsMenuItem.getText(): ",
                leftHandName, resultsMenuItem.getText());
    }   //testCheckUnConnectedTracks

    @Test
    public void testCheckUnBlockedTracks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("toolsMenuTitle not null", toolsMenuTitle);
        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        Assert.assertNotNull("CheckMenuTitle not null", checkMenuTitle);
        Assert.assertNotNull("checkUnBlockedTracksMenuTitle not null", checkUnBlockedTracksMenuTitle);
        toolsJMO.pushMenuNoBlock(toolsMenuTitle + "/" + checkMenuTitle + "/"
                + checkUnBlockedTracksMenuTitle, "/");
        new QueueTool().waitEmpty();

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
        checkUnBlockedTracksJMO.push();
        JPopupMenu checkUnBlockedTracksPopupMenu = checkUnBlockedTracksJMO.getPopupMenu();

        //verify results
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
        Assert.assertNotNull("toolsMenuTitle not null", toolsMenuTitle);
        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        Assert.assertNotNull("CheckMenuTitle not null", checkMenuTitle);
        Assert.assertNotNull("checkNonContiguousBlocksMenuTitle not null", checkNonContiguousBlocksMenuTitle);
        toolsJMO.pushMenuNoBlock(toolsMenuTitle + "/" + checkMenuTitle + "/"
                + checkNonContiguousBlocksMenuTitle, "/");
        new QueueTool().waitEmpty();

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
        checkNonContiguousBlocksJMO.push();

        JPopupMenu checkNonContiguousBlocksPopupMenu = checkNonContiguousBlocksJMO.getPopupMenu();

        //verify results
        JMenuItem resultsMenuItem = (JMenuItem) checkNonContiguousBlocksPopupMenu.getComponent(0);
        Assert.assertEquals("resultsMenuItem0.getText(): ", myBlockName, resultsMenuItem.getText());

        JMenuOperator resultsJMO = new JMenuOperator((JMenu) resultsMenuItem);
        JPopupMenu resultsPopupMenu = resultsJMO.getPopupMenu();
        JMenuItem resultsSubMenuItem = (JMenuItem) resultsPopupMenu.getComponent(0);
        Assert.assertEquals("resultsSubMenuItem0.getText(): ", myBlockName + ":#1", resultsSubMenuItem.getText());
        resultsSubMenuItem = (JMenuItem) resultsPopupMenu.getComponent(1);
        Assert.assertEquals("resultsSubMenuItem1.getText(): ", myBlockName + ":#2", resultsSubMenuItem.getText());
    }   //testCheckNonContiguousBlocks

    @Test
    public void testCheckUnnecessaryAnchors() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("toolsMenuTitle not null", toolsMenuTitle);
        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        Assert.assertNotNull("CheckMenuTitle not null", checkMenuTitle);
        Assert.assertNotNull("checkUnnecessaryAnchorsMenuTitle not null", checkUnnecessaryAnchorsMenuTitle);
        toolsJMO.pushMenuNoBlock(toolsMenuTitle + "/" + checkMenuTitle + "/"
                + checkUnnecessaryAnchorsMenuTitle, "/");
        new QueueTool().waitEmpty();

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
        checkUnnecessaryAnchorsJMO.push();

        JPopupMenu checkUnnecessaryAnchorsPopupMenu = checkUnnecessaryAnchorsJMO.getPopupMenu();

        //verify results
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

            layoutEditor = new LayoutEditor("Layout Editor Checks Test Layout");
            layoutEditor.setPanelBounds(new Rectangle2D.Double(0, 0, 640, 480));
            layoutEditor.setVisible(true);
            layoutEditorChecks = layoutEditor.getLEChecks();

            layoutEditorEFO = new EditorFrameOperator(layoutEditor);

            Point2D point = new Point2D.Double(150.0, 100.0);
            Point2D delta = new Point2D.Double(100.0, 50.0);
            List<LayoutTrack> layoutTracks = layoutEditor.getLayoutTracks();
            Assert.assertNotNull("layoutTracks not null", layoutTracks);

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
        }
        JUnitUtil.tearDown();
    }

//    @Before
//    public void setUp() {
//        JUnitUtil.setUp();
//        if (!GraphicsEnvironment.isHeadless()) {
//            JUnitUtil.resetProfileManager();
//        }
//    }
//
//    @After
//    public void tearDown() {
//        JUnitUtil.tearDown();
//    }
//
//  TODO: remove or comment out for production
//
    private static int indent_spaces = 1;

    private void dumpMenuElement(MenuElement menuElement) {
        System.out.println(StringUtils.leftPad(((JMenuItem) menuElement).getText(), indent_spaces, " "));
        indent_spaces += 4;
        for (MenuElement subMenuElement : menuElement.getSubElements()) {
            dumpMenuElement(subMenuElement);
        }
        indent_spaces -= 4;
    }

    private static void dumpMenuBar(JMenuBar menuBar) {
        for (MenuElement element : menuBar.getSubElements()) {
            dumpMenuElements(element);
        }
    }

    private static void dumpMenuElements(MenuElement menuElement) {
        if (menuElement instanceof JMenuItem) {
            System.out.println(StringUtils.leftPad("", indent_spaces * 2)
                    + ((JMenuItem) menuElement).getText());
        }
        for (MenuElement subElement : menuElement.getSubElements()) {
            indent_spaces++;
            dumpMenuElements(subElement);
            indent_spaces--;
        }
    }
//
//    private void dumpScreen() {
//        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
//        PNGEncoder.captureScreen(desktopPath + File.separator + "screen.png");
//        try {
//            Dumper.dumpAll(desktopPath + File.separator + "screen.xml");
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(LayoutEditorChecksTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
    //private transient final static Logger log = LoggerFactory.getLogger(LayoutEditorChecksTest.class);
}
