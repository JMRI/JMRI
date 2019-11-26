package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.JUnitUtil;
import jmri.util.MathUtil;
import jmri.util.junit.rules.RetryRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.operators.JMenuOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author George Warner Copyright: (C) 2019
 */
public class LayoutEditorChecksTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 second timeout for methods in this test class.

    @Rule    // allow 2 retries of intermittent tests
    public RetryRule retryRule = new RetryRule(2); // allow 2 retries

    //LayoutEditorChecks Bundle Strings
    String toolsMenuTitle = Bundle.getMessage("MenuTools");
    String checkMenuTitle = Bundle.getMessage("CheckMenuTitle");
    String checkUnConnectedTracksMenuTitle = Bundle.getMessage("CheckUnConnectedTracksMenuTitle");
    String checkUnBlockedTracksMenuTitle = Bundle.getMessage("CheckUnBlockedTracksMenuTitle");
    String checkNonContiguousBlocksMenuTitle = Bundle.getMessage("CheckNonContiguousBlocksMenuTitle");
    String checkUnnecessaryAnchorsMenuTitle = Bundle.getMessage("CheckUnnecessaryAnchorsMenuTitle");

    private LayoutEditor layoutEditor = null;
    private LayoutEditorChecks layoutEditorChecks = null;
    private LayoutTurnout ltRH = null, ltLH = null;
    //private PositionablePoint pp = null;

    private EditorFrameOperator layoutEditorEFO = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", layoutEditorChecks);
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
    public void checkToolsMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        toolsJMO.pushMenu(toolsMenuTitle + "/" + checkMenuTitle, "/");
//        Assert.assertEquals("Menu Item Count", 17, checkJMO.getItemCount());
    }

    @Test
    public void testCheckUnConnectedTracks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        toolsJMO.pushMenu(toolsMenuTitle + "/" + checkMenuTitle + "/"
                + checkUnConnectedTracksMenuTitle, "/");

        JPopupMenu toolsPopupMenu = toolsJMO.getPopupMenu();
        JMenuItem checkMenuItem = (JMenuItem) toolsPopupMenu.getComponent(0);
        //log.error("firstToolsMenuItem.getText(): " + firstMenuItem.getText());
        Assert.assertEquals("firstToolsMenuItem.getText(): ",
                checkMenuTitle, checkMenuItem.getText());

        JMenuOperator checkMO = new JMenuOperator((JMenu) checkMenuItem);
        JPopupMenu checkPopupMenu = checkMO.getPopupMenu();
        JMenuItem checkSubMenuItem = (JMenuItem) checkPopupMenu.getComponent(0);
        Assert.assertEquals("firstCheckMenuItem.getText(): ",
                checkUnConnectedTracksMenuTitle,
                checkSubMenuItem.getText());
        //TODO:validate results
    }

    @Test
    public void testCheckUnBlockedTracks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        toolsJMO.pushMenu(toolsMenuTitle + "/" + checkMenuTitle + "/"
                + checkUnBlockedTracksMenuTitle, "/");

        JPopupMenu toolsPopupMenu = toolsJMO.getPopupMenu();
        JMenuItem checkMenuItem = (JMenuItem) toolsPopupMenu.getComponent(0);
        //log.error("firstToolsMenuItem.getText(): " + firstMenuItem.getText());
        Assert.assertEquals("firstToolsMenuItem.getText(): ",
                checkMenuTitle, checkMenuItem.getText());

        JMenuOperator checkMO = new JMenuOperator((JMenu) checkMenuItem);
        JPopupMenu checkPopupMenu = checkMO.getPopupMenu();
        JMenuItem checkSubMenuItem = (JMenuItem) checkPopupMenu.getComponent(1);
        Assert.assertEquals("firstCheckMenuItem.getText(): ",
                checkUnBlockedTracksMenuTitle,
                checkSubMenuItem.getText());
        //TODO:validate results
    }

    @Test
    public void testCheckNonContiguousBlocks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        toolsJMO.pushMenu(toolsMenuTitle + "/" + checkMenuTitle + "/"
                + checkNonContiguousBlocksMenuTitle, "/");

        JPopupMenu toolsPopupMenu = toolsJMO.getPopupMenu();
        JMenuItem checkMenuItem = (JMenuItem) toolsPopupMenu.getComponent(0);
        //log.error("firstToolsMenuItem.getText(): " + firstMenuItem.getText());
        Assert.assertEquals("firstToolsMenuItem.getText(): ",
                checkMenuTitle, checkMenuItem.getText());

        JMenuOperator checkMO = new JMenuOperator((JMenu) checkMenuItem);
        JPopupMenu checkPopupMenu = checkMO.getPopupMenu();
        JMenuItem checkSubMenuItem = (JMenuItem) checkPopupMenu.getComponent(2);
        Assert.assertEquals("firstCheckMenuItem.getText(): ",
                checkNonContiguousBlocksMenuTitle,
                checkSubMenuItem.getText());
        //TODO:validate results
    }

    @Test
    public void testCheckUnnecessaryAnchors() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JMenuOperator toolsJMO = new JMenuOperator(layoutEditorEFO, toolsMenuTitle);
        toolsJMO.pushMenu(toolsMenuTitle + "/" + checkMenuTitle + "/"
                + checkUnnecessaryAnchorsMenuTitle, "/");

        JPopupMenu toolsPopupMenu = toolsJMO.getPopupMenu();
        JMenuItem checkMenuItem = (JMenuItem) toolsPopupMenu.getComponent(0);
        //log.error("firstToolsMenuItem.getText(): " + firstMenuItem.getText());
        Assert.assertEquals("firstToolsMenuItem.getText(): ",
                checkMenuTitle, checkMenuItem.getText());

        JMenuOperator checkMO = new JMenuOperator((JMenu) checkMenuItem);
        JPopupMenu checkPopupMenu = checkMO.getPopupMenu();

        JMenuItem checkSubMenuItem = (JMenuItem) checkPopupMenu.getComponent(3);
        Assert.assertEquals("firstCheckMenuItem.getText(): ",
                checkUnnecessaryAnchorsMenuTitle,
                checkSubMenuItem.getText());
        //TODO:validate results
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        if (!GraphicsEnvironment.isHeadless()) {
            layoutEditor = new LayoutEditor("Layout Editor Checks Test Layout");
            layoutEditor.setPanelBounds(new Rectangle2D.Double(0, 0, 640, 480));
            layoutEditor.setVisible(true);
            layoutEditorEFO = new EditorFrameOperator(layoutEditor);
            layoutEditorChecks = layoutEditor.getLEChecks();

            Point2D point = new Point2D.Double(150.0, 100.0);
            Point2D delta = new Point2D.Double(50.0, 75.0);

            ltRH = new LayoutTurnout("Right Hand", LayoutTurnout.RH_TURNOUT,
                    point, 33.0, 1.1, 1.2, layoutEditor);
            layoutEditor.getLayoutTracks().add(ltRH);

            point = MathUtil.add(point, delta);
            //PositionablePoint pp = new PositionablePoint("A1", ANCHOR, point, layoutEditor);
            //Assert.assertNotNull("PositionablePoint", pp);
            //layoutEditor.getLayoutTracks().add(pp);

            point = MathUtil.add(point, delta);
            ltLH = new LayoutTurnout("Left Hand", LayoutTurnout.LH_TURNOUT,
                    point, 66.0, 1.3, 1.4, layoutEditor);
            layoutEditor.getLayoutTracks().add(ltLH);
        }
    }

    @After
    public void tearDown() {
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.dispose(layoutEditor);
            layoutEditor = null;
            layoutEditorChecks = null;
        }
        JUnitUtil.tearDown();
    }
    //private transient final static Logger log = LoggerFactory.getLogger(LayoutEditorChecksTest.class);
}
