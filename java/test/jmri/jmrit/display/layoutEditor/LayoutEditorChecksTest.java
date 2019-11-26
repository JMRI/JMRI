package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import jmri.InstanceManager;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.JUnitUtil;
import jmri.util.MathUtil;
import jmri.util.junit.rules.RetryRule;
import org.junit.*;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.util.Dumper;
import org.netbeans.jemmy.util.PNGEncoder;

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

    private static EditorFrameOperator layoutEditorEFO = null;

    private static LayoutEditor layoutEditor = null;
    private static LayoutEditorChecks layoutEditorChecks = null;
    private static LayoutBlock layoutBlock = null;
    private static LayoutTurnout ltRH = null, ltLH = null;
    private static PositionablePoint pp = null;
    private static TrackSegment ts1 = null;
    private static TrackSegment ts2 = null;

    ///@Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", layoutEditorChecks);
    }

    ///@Test
    public void testSetup() {
        Assert.assertNotNull("layoutEditor not null", layoutEditor);
        Assert.assertNotNull("layoutBlock not null", layoutBlock);
        Assert.assertNotNull("ltRH not null", ltRH);
        Assert.assertNotNull("ltLH not null", ltLH);
        Assert.assertNotNull("pp not null", pp);
        Assert.assertNotNull("ts1 not null", ts1);
        Assert.assertNotNull("ts2 not null", ts2);
    }

    ///@Test
    public void testBundleStrings() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertNotNull("toolsMenuTitle not null", toolsMenuTitle);
        Assert.assertNotNull("CheckMenuTitle not null", checkMenuTitle);

        Assert.assertNotNull("checkUnConnectedTracksMenuTitle not null", checkUnConnectedTracksMenuTitle);
        Assert.assertNotNull("checkUnBlockedTracksMenuTitle not null", checkUnBlockedTracksMenuTitle);
        Assert.assertNotNull("checkNonContiguousBlocksMenuTitle not null", checkNonContiguousBlocksMenuTitle);
        Assert.assertNotNull("checkUnnecessaryAnchorsMenuTitle not null", checkUnnecessaryAnchorsMenuTitle);
    }

    ///@Test
    public void checkToolsMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        toolsJMO.pushMenuNoBlock(toolsMenuTitle + "/" + checkMenuTitle, "/");
///        Assert.assertEquals("Menu Item Count", 17, checkJMO.getItemCount());
    }

    @Test
    public void testFoo() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        String paths[] = {toolsMenuTitle, checkMenuTitle, checkUnConnectedTracksMenuTitle, "TO1"};
        toolsJMO.showMenuItem(paths);
        dumpScreen();
    }

    ///@Test
    public void testCheckUnConnectedTracks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        toolsJMO.pushMenuNoBlock(toolsMenuTitle + "/" + checkMenuTitle + "/"
                + checkUnConnectedTracksMenuTitle, "/");

        new QueueTool().waitEmpty(20);

        JPopupMenu toolsPopupMenu = toolsJMO.getPopupMenu();
        JMenuItem checkMenuItem = (JMenuItem) toolsPopupMenu.getComponent(0);
        Assert.assertEquals("checkMenuItem.getText(): ",
                checkMenuTitle, checkMenuItem.getText());

        JMenuOperator checkMO = new JMenuOperator((JMenu) checkMenuItem);
        JPopupMenu checkPopupMenu = checkMO.getPopupMenu();

        JMenuItem checkSubMenuItem = (JMenuItem) checkPopupMenu.getComponent(0);
        Assert.assertEquals("checkSubMenuItem.getText(): ",
                checkUnConnectedTracksMenuTitle,
                checkSubMenuItem.getText());
        //TODO:validate results
//        JMenuOperator checkSubMenuMO = new JMenuOperator((JMenu) checkSubMenuItem);
//        JPopupMenu checkSubMenuPopupMenu = checkSubMenuMO.getPopupMenu();
//        JMenuItem resultsSubMenuItem = (JMenuItem) checkSubMenuPopupMenu.getComponent(0);
//        dumpMenuElement(checkSubMenuItem);
//        MenuElement resultsMenuElements[] = checkSubMenuItem.getSubElements();
//        int i = 0;
//        for (MenuElement resultsMenuElement : resultsMenuElements) {
//            System.out.println("resultsMenuElements[" + i++ + "]: " + resultsMenuElement.toString());
//        }
//        JMenuItem resultsSubMenuItem = (JMenuItem) checkSubMenuItem.getComponent(0);
//        Assert.assertEquals("resultsSubMenuItem.getText(): ",
//                "Mud", resultsMenuElements[0].getText());
    }   //testCheckUnConnectedTracks

    ///@Test
    public void testCheckUnBlockedTracks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        toolsJMO.pushMenuNoBlock(toolsMenuTitle + "/" + checkMenuTitle + "/"
                + checkUnBlockedTracksMenuTitle, "/");

        JPopupMenu toolsPopupMenu = toolsJMO.getPopupMenu();
        JMenuItem checkMenuItem = (JMenuItem) toolsPopupMenu.getComponent(0);
        Assert.assertEquals("checkMenuItem.getText(): ",
                checkMenuTitle, checkMenuItem.getText());

        JMenuOperator checkMO = new JMenuOperator((JMenu) checkMenuItem);
        JPopupMenu checkPopupMenu = checkMO.getPopupMenu();

        JMenuItem checkSubMenuItem = (JMenuItem) checkPopupMenu.getComponent(1);
        Assert.assertEquals("checkSubMenuItem.getText(): ",
                checkUnBlockedTracksMenuTitle,
                checkSubMenuItem.getText());
        //TODO:validate results
    }   //testCheckUnBlockedTracks

    ///@Test
    public void testCheckNonContiguousBlocks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        toolsJMO.pushMenuNoBlock(toolsMenuTitle + "/" + checkMenuTitle + "/"
                + checkNonContiguousBlocksMenuTitle, "/");

        JPopupMenu toolsPopupMenu = toolsJMO.getPopupMenu();
        JMenuItem checkMenuItem = (JMenuItem) toolsPopupMenu.getComponent(0);
        Assert.assertEquals("checkMenuItem.getText(): ",
                checkMenuTitle, checkMenuItem.getText());

        JMenuOperator checkMO = new JMenuOperator((JMenu) checkMenuItem);
        JPopupMenu checkPopupMenu = checkMO.getPopupMenu();

        JMenuItem checkSubMenuItem = (JMenuItem) checkPopupMenu.getComponent(2);
        Assert.assertEquals("checkSubMenuItem.getText(): ",
                checkNonContiguousBlocksMenuTitle,
                checkSubMenuItem.getText());
        //TODO:validate results
    }   //testCheckNonContiguousBlocks

    ///@Test
    public void testCheckUnnecessaryAnchors() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        toolsJMO.pushMenuNoBlock(toolsMenuTitle + "/" + checkMenuTitle + "/"
                + checkUnnecessaryAnchorsMenuTitle, "/");

        JPopupMenu toolsPopupMenu = toolsJMO.getPopupMenu();
        JMenuItem checkMenuItem = (JMenuItem) toolsPopupMenu.getComponent(0);
        Assert.assertEquals("checkMenuItem.getText(): ",
                checkMenuTitle, checkMenuItem.getText());

        JMenuOperator checkMO = new JMenuOperator((JMenu) checkMenuItem);
        JPopupMenu checkPopupMenu = checkMO.getPopupMenu();

        JMenuItem checkSubMenuItem = (JMenuItem) checkPopupMenu.getComponent(3);
        Assert.assertEquals("checkSubMenuItem.getText(): ",
                checkUnnecessaryAnchorsMenuTitle,
                checkSubMenuItem.getText());
        //TODO:validate results
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
            Point2D delta = new Point2D.Double(50.0, 75.0);
            List<LayoutTrack> layoutTracks = layoutEditor.getLayoutTracks();
            Assert.assertNotNull("layoutTracks not null", layoutTracks);

            //create a layout block
            layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, "My Block");

            //add 1st turnout
            ltRH = new LayoutTurnout("Right Hand", LayoutTurnout.RH_TURNOUT,
                    point, 33.0, 1.1, 1.2, layoutEditor);
            layoutTracks.add(ltRH);
            ltRH.setLayoutBlock(layoutBlock);

            //add an anchor
            point = MathUtil.add(point, delta);
            pp = new PositionablePoint("A1", PositionablePoint.ANCHOR, point, layoutEditor);
            Assert.assertNotNull("PositionablePoint", pp);
            layoutTracks.add(pp);

            //connect 1st turnout to anchor
            ts1 = addNewTrackSegment(ltRH, LayoutTrack.TURNOUT_B, pp, LayoutTrack.POS_POINT);

            //add 2nd turnout
            point = MathUtil.add(point, delta);
            ltLH = new LayoutTurnout("Left Hand", LayoutTurnout.LH_TURNOUT,
                    point, 66.0, 1.3, 1.4, layoutEditor);
            layoutTracks.add(ltLH);
            ltLH.setLayoutBlock(layoutBlock);

            //connect anchor to 2nd turnout
            ts2 = addNewTrackSegment(pp, LayoutTrack.POS_POINT, ltLH, LayoutTrack.TURNOUT_A);

            //wait for layout editor to finish drawing
            new QueueTool().waitEmpty(20);
        }
    }

    private static TrackSegment addNewTrackSegment(
            @CheckForNull LayoutTrack c1, int t1,
            @CheckForNull LayoutTrack c2, int t2) {
        TrackSegment result = null;
        if ((c1 != null) && (c2 != null)) {
            //create new track segment
            String name = layoutEditor.getFinder().uniqueName("T", 1);
            result = new TrackSegment(name, c1, t1, c2, t2,
                    false, false, layoutEditor);
            layoutEditor.getLayoutTracks().add(ts1);
            //link to connected objects
            layoutEditor.setLink(c1, t1, result, LayoutTrack.TRACK);
            layoutEditor.setLink(c2, t2, result, LayoutTrack.TRACK);
        }
        return result;
    }

    @AfterClass
    public static void tearDownClass() {
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.dispose(layoutEditor);
            layoutEditor = null;
            layoutEditorChecks = null;
            layoutEditorEFO.requestClose();
            layoutEditorEFO.waitClosed();
        }
        JUnitUtil.tearDown();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();
        }
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    //TODO: remove or comment out for production
    private int indent_level = 0;

    private void dumpMenuElement(MenuElement jMenuElement) {
        String tabs = "";
        for (int i = 0; i < indent_level; i++) {
            tabs += " ";
        }
        System.out.println(tabs + "jMenuElement.getText(): " + ((JMenu) jMenuElement).getText());
        indent_level += 4;
        MenuElement resultsMenuElements[] = jMenuElement.getSubElements();
        for (MenuElement resultsMenuElement : resultsMenuElements) {
            dumpMenuElement(resultsMenuElement);
        }
        indent_level -= 4;
    }

    private void dumpScreen() {
        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
        PNGEncoder.captureScreen(desktopPath + File.separator + "screen.png");
        try {
            Dumper.dumpAll(desktopPath + File.separator + "screen.xml");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LayoutEditorChecksTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //private transient final static Logger log = LoggerFactory.getLogger(LayoutEditorChecksTest.class);
}
