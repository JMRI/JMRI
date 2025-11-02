package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Toolkit;
import java.io.File;

import jmri.*;
import jmri.jmrit.display.*;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;
import jmri.util.ThreadingUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JMenuOperator;

/**
 * Test simple functioning of LayoutEditor.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author George Warner Copyright (C) 2019
 * @author Bob Jacobsen Copyright (C) 2020
 */
@Timeout(10)
@jmri.util.junit.annotations.DisabledIfHeadless
public class LayoutEditorTest extends AbstractEditorTestBase<LayoutEditor> {

    private EditorFrameOperator jfo = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLayoutBlockManager();

        e = new LayoutEditor("Layout Editor Test Layout");
        ThreadingUtil.runOnGUI( () -> e.setVisible(true));
        jfo = new EditorFrameOperator(e);

    }

    @AfterEach
    @Override
    public void tearDown() {
        if (e != null && jfo != null) {
            jfo.closeFrameWithConfirmations();
            e = null;
        }
        jmri.jmrit.display.EditorFrameOperator.clearEditorFrameOperatorThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    @Test
    public void testStringCtor() {
        Assertions.assertNotNull(jfo);
        jfo.closeFrameWithConfirmations(); // close layoutEditor created in setUp
        LayoutEditor stringCtorEditor = new LayoutEditor("Layout Editor String Constructor");
        Assert.assertNotNull("exists", stringCtorEditor);
        JUnitUtil.dispose(stringCtorEditor);
    }

    @Test
    public void testDefaultCtor() {
        Assertions.assertNotNull(jfo);
        jfo.closeFrameWithConfirmations(); // close layoutEditor created in setUp
        LayoutEditor defaultCtorEditor = new LayoutEditor(); // create layout editor
        Assert.assertNotNull("exists", defaultCtorEditor);
        JUnitUtil.dispose(defaultCtorEditor);
    }

    @Test
    @Disabled("Test fails to find and close dialog on Jenkins")
    public void testSavePanel() throws Exception {

        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuFile"));

        //delete this file so we won't get the "<xxx> exists... do you want to replace?" dialog.
        Assertions.assertTrue(new File("temp/Layout Editor Test Layout.xml").delete());

        // test the file -> delete panel menu item
        Thread misc1 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("FileMenuItemStore"),
                Bundle.getMessage("ButtonCancel"));  // NOI18N
        jmo.pushMenu(Bundle.getMessage("MenuFile") + "/"
                + Bundle.getMessage("FileMenuItemStore"), "/");
        JUnitUtil.waitFor(() -> {
            return !(misc1.isAlive());
        }, "misc1 finished");

        //clean up after ourselves...
        Assertions.assertTrue(new File("temp/Layout Editor Test Layout.xml").delete());
    }

    @Test
    @Disabled("Test fails to find and close dialog on Jenkins")
    public void testDeletePanel() {

        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuFile"));

        // test the file -> delete panel menu item
        Thread misc1 = JemmyUtil.createModalDialogOperatorThread(
                Bundle.getMessage("DeleteVerifyTitle"),
                Bundle.getMessage("ButtonYesDelete"));  // NOI18N
        jmo.pushMenu(Bundle.getMessage("MenuFile") + "/"
                + Bundle.getMessage("DeletePanel"), "/");
        JUnitUtil.waitFor(() -> {
            return !(misc1.isAlive());
        }, "misc1 finished");
        JUnitUtil.dispose(e);
        e = null; // prevent closing the window using the operator in shutDown.
    }

    @Test
    public void testGetFinder() {
        LayoutEditorFindItems f = e.getFinder();
        Assert.assertNotNull("exists", f);
    }

    @Test
    @Override
    @Disabled("failing to set size on appveyor")
    public void testSetSize() {
        e.setSize(100, 100);
        java.awt.Dimension d = e.getSize();

        // the java.awt.Dimension stores the values as floating point
        // numbers, but setSize expects integer parameters.
        Assert.assertEquals("Width Set", 100.0, d.getWidth(), 0.0);
        Assert.assertEquals("Height Set", 100.0, d.getHeight(), 0.0);
    }

    @Test
    @Disabled("Failing to set second zoom")
    public void testGetSetZoom() {
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((m) -> {
            m.setSaveAllowed(false); // prevent attempts to save while zooming in rest of test
        });
        Assert.assertEquals("Get initial Zoom", 1.0, e.getZoom(), 0.0);

        // note: Layout Editor won't allow zooms above 8.0.
        e.setZoom(10.0);
        Assert.assertEquals("Get Zoom after set above max", 8.0, e.getZoom(), 0.0);
        e.setZoom(3.33);
        Assert.assertEquals("Get Zoom After Set to 3.33", 3.33, e.getZoom(), 0.0);
    }

    @Test
    public void testGetOpenDispatcherOnLoad() {
        // defaults to false.
        Assert.assertFalse("getOpenDispatcherOnLoad", e.getOpenDispatcherOnLoad());
    }

    @Test
    public void testSetOpenDispatcherOnLoad() {
        // defaults to false, so set to true.
        e.setOpenDispatcherOnLoad(true);
        Assert.assertTrue("setOpenDispatcherOnLoad after set", e.getOpenDispatcherOnLoad());
    }

    @Test
    public void testIsDirty() {
        // defaults to false.
        Assert.assertFalse("isDirty", e.isDirty());
    }

    @Test
    public void testSetDirty() {
        // defaults to false, setDirty() sets it to true.
        e.setDirty();
        Assert.assertTrue("isDirty after set", e.isDirty());
    }

    @Test
    public void testSetDirtyWithParameter() {
        // defaults to false, so set it to true.
        e.setDirty(true);
        Assert.assertTrue("isDirty after set", e.isDirty());
    }

    @Test
    public void testResetDirty() {
        // defaults to false, so set it to true.
        e.setDirty(true);
        // then call resetDirty, which sets it back to false.
        e.resetDirty();
        Assert.assertFalse("isDirty after reset", e.isDirty());
    }

    @Test
    public void testIsAnimating() {
        // default to true
        Assert.assertTrue("isAnimating", e.isAnimating());
    }

    @Test
    public void testSetTurnoutAnimating() {
        // default to true, so set to false.
        e.setTurnoutAnimation(false);
        Assert.assertFalse("isAnimating after set", e.isAnimating());
    }

    @Test
    public void testGetLayoutWidth() {
        // defaults to 0
        Assert.assertEquals("layout width", 0, e.gContext.getLayoutWidth());
    }

    @Test
    public void testGetLayoutHeight() {
        // defaults to 0
        Assert.assertEquals("layout height", 0, e.gContext.getLayoutHeight());
    }

    @Test
    public void testGetWindowWidth() {
        // defaults to screen width - 20
        int w = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 20);
        Assert.assertEquals("window width", w, e.gContext.getWindowWidth());
    }

    @Test
    public void testGetWindowHeight() {
        // defaults to screen height - 120
        int h = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 120);
        Assert.assertEquals("window height", h, e.gContext.getWindowHeight());
    }

    @Test
    public void testGetUpperLeftX() {
        // defaults to 0
        Assert.assertEquals("upper left X", 0, e.gContext.getUpperLeftX());
    }

    @Test
    public void testGetUpperLeftY() {
        // defaults to 0
        Assert.assertEquals("upper left Y", 0, e.gContext.getUpperLeftY());
    }

    @Test
    public void testSetLayoutDimensions() {
        ThreadingUtil.runOnGUI(() -> {
            // set the panel dimensions to known values
            e.setLayoutDimensions(100, 100, 100, 100, 100, 100);
            Assert.assertEquals("layout width after set", 100, e.gContext.getLayoutWidth());
            Assert.assertEquals("layout height after set", 100, e.gContext.getLayoutHeight());
            Assert.assertEquals("window width after set", 100, e.gContext.getWindowWidth());
            Assert.assertEquals("window height after set", 100, e.gContext.getWindowHeight());
            Assert.assertEquals("upper left X after set", 100, e.gContext.getUpperLeftX());
            Assert.assertEquals("upper left Y after set", 100, e.gContext.getUpperLeftX());
        });
    }

    @Test
    public void testSetGrideSize() {
        Assert.assertEquals("grid size after set", 100, e.gContext.setGridSize(100));
    }

    @Test
    public void testGetGrideSize() {
        // defaults to 10.
        Assert.assertEquals("grid size", 10, e.gContext.getGridSize());
    }

    @Test
    public void testGetMainlineTrackWidth() {
        // defaults to 4.
        Assert.assertEquals("mainline track width", 4, e.gContext.getMainlineTrackWidth());
    }

    @Test
    public void testSetMainlineTrackWidth() {
        // set to known value
        e.gContext.setMainlineTrackWidth(10);
        Assert.assertEquals("mainline track width after set", 10, e.gContext.getMainlineTrackWidth());
    }

    @Test
    public void testGetSidelineTrackWidth() {
        // defaults to 2.
        Assert.assertEquals("side track width", 2, e.gContext.getSidelineTrackWidth());
    }

    @Test
    public void testSetSideTrackWidth() {
        // set to known value
        e.gContext.setSidelineTrackWidth(10);
        Assert.assertEquals("Side track width after set", 10, e.gContext.getSidelineTrackWidth());
    }

    @Test
    public void testGetXScale() {
        // defaults to 1.
        Assert.assertEquals("XScale", 1.0, e.gContext.getXScale(), 0.0);
    }

    @Test
    public void testSetXScale() {
        // set to known value
        e.gContext.setXScale(2.0);
        Assert.assertEquals("XScale after set ", 2.0, e.gContext.getXScale(), 0.0);
    }

    @Test
    public void testGetYScale() {
        // defaults to 1.
        Assert.assertEquals("YScale", 1.0, e.gContext.getYScale(), 0.0);
    }

    @Test
    public void testSetYScale() {
        // set to known value
        e.gContext.setYScale(2.0);
        Assert.assertEquals("YScale after set ", 2.0, e.gContext.getYScale(), 0.0);
    }

    @Test
    public void testGetDefaultTrackColor() {
        Assert.assertEquals("Default Track Color", ColorUtil.ColorDarkGray, e.getDefaultTrackColor());
    }

    @Test
    public void testSetDefaultTrackColor() {
        e.setDefaultTrackColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Default Track Color after Set", ColorUtil.ColorPink, e.getDefaultTrackColor());
    }

    @Test
    public void testGetDefaultOccupiedTrackColor() {
        Assert.assertEquals("Default Occupied Track Color", "red", e.getDefaultOccupiedTrackColor());
    }

    @Test
    public void testSetDefaultOccupiedTrackColor() {
        e.setDefaultOccupiedTrackColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Default Occupied Track Color after Set", ColorUtil.ColorPink, e.getDefaultOccupiedTrackColor());
    }

    @Test
    public void testGetDefaultAlternativeTrackColor() {
        Assert.assertEquals("Default Alternative Track Color", ColorUtil.ColorWhite, e.getDefaultAlternativeTrackColor());
    }

    @Test
    public void testSetDefaultAlternativeTrackColor() {
        e.setDefaultAlternativeTrackColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Default Alternative Track Color after Set", ColorUtil.ColorPink, e.getDefaultAlternativeTrackColor());
    }

    @Test
    public void testSetAllTracksToDefaultColors() {

        LayoutBlock layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock("ILB999", "Test Block");
        Assert.assertNotNull("layoutBlock created", layoutBlock);

        Assert.assertEquals("BlockTrackColor default", e.getDefaultTrackColorColor(), layoutBlock.getBlockTrackColor());
        layoutBlock.setBlockTrackColor(Color.pink);
        Assert.assertEquals("BlockTrackColor set to pink", Color.pink, layoutBlock.getBlockTrackColor());

        Assert.assertEquals("BlockOccupiedColor default", e.getDefaultOccupiedTrackColorColor(), layoutBlock.getBlockOccupiedColor());
        layoutBlock.setBlockOccupiedColor(Color.pink);
        Assert.assertEquals("BlockOccupiedColor set to pink", Color.pink, layoutBlock.getBlockOccupiedColor());

        Assert.assertEquals("BlockExtraColor default", e.getDefaultAlternativeTrackColorColor(), layoutBlock.getBlockExtraColor());
        layoutBlock.setBlockExtraColor(Color.pink);
        Assert.assertEquals("BlockExtraColor set to pink", Color.pink, layoutBlock.getBlockExtraColor());

        int changed = e.setAllTracksToDefaultColors();
        Assert.assertEquals("setAllTracksToDefaultColors changed one block", 1, changed);

        Assert.assertEquals("BlockTrackColor back to default", e.getDefaultTrackColorColor(), layoutBlock.getBlockTrackColor());
        Assert.assertEquals("BlockOccupiedColor back to default", e.getDefaultOccupiedTrackColorColor(), layoutBlock.getBlockOccupiedColor());
        Assert.assertEquals("BlockExtraColor back to default", e.getDefaultAlternativeTrackColorColor(), layoutBlock.getBlockExtraColor());
    }

    @Test
    public void testGetDefaultTextColor() {
        Assert.assertEquals("Default Text Color", ColorUtil.ColorBlack, e.getDefaultTextColor());
    }

    @Test
    public void testSetDefaultTextColor() {
        e.setDefaultTextColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Default Text Color after Set", ColorUtil.ColorPink, e.getDefaultTextColor());
    }

    @Test
    public void testGetTurnoutCircleColor() {
        Assert.assertEquals("Turnout Circle Color", ColorUtil.ColorBlack, e.getTurnoutCircleColor());
    }

    @Test
    public void testSetTurnoutCircleColor() {
        e.setTurnoutCircleColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Turnout Circle after Set", ColorUtil.ColorPink, e.getTurnoutCircleColor());
    }

    @Test
    public void testGetTurnoutCircleThrownColor() {
        Assert.assertEquals("Turnout Circle Thrown Color", ColorUtil.ColorBlack, e.getTurnoutCircleThrownColor());
    }

    @Test
    public void testSetTurnoutCircleThrownColor() {
        e.setTurnoutCircleThrownColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        Assert.assertEquals("Turnout Circle after Set", ColorUtil.ColorPink, e.getTurnoutCircleThrownColor());
    }

    @Test
    public void testIsTurnoutFillControlCircles() {
        // default to false
        Assert.assertFalse("isTurnoutFillControlCircles", e.isTurnoutFillControlCircles());
    }

    @Test
    public void testSetTurnoutFillControlCircles() {
        // default to false, so set to true.
        e.setTurnoutFillControlCircles(true);
        Assert.assertTrue("isTurnoutFillControlCircles after set true", e.isTurnoutFillControlCircles());
        // set back to default (false) and confirm new value
        e.setTurnoutFillControlCircles(false);
        Assert.assertFalse("isTurnoutFillControlCircles after set false", e.isTurnoutFillControlCircles());
    }

    @Test
    public void testGetTurnoutCircleSize() {
        // defaults to 4.
        Assert.assertEquals("turnout circle size", 4, e.getTurnoutCircleSize());
    }

    @Test
    public void testSetTurnoutCircleSize() {
        e.setTurnoutCircleSize(11);
        Assert.assertEquals("turnout circle size after set", 11, e.getTurnoutCircleSize());
    }

    @Test
    public void testGetTurnoutDrawUnselectedLeg() {
        // default to true
        Assert.assertTrue("getTurnoutDrawUnselectedLeg", e.isTurnoutDrawUnselectedLeg());
    }

    @Test
    public void testSetTurnoutDrawUnselectedLeg() {
        // default to true, so set to false.
        e.setTurnoutDrawUnselectedLeg(false);
        Assert.assertFalse("getTurnoutDrawUnselectedLeg after set", e.isTurnoutDrawUnselectedLeg());
    }

    @Test
    public void testGetLayoutName() {
        e.dispose(); // remove existing instance
        e = new LayoutEditor(); // create new instance to test the default name
        jfo = new EditorFrameOperator(e);
        // default is "My Layout"
        Assert.assertEquals("getLayoutName", "My Layout", e.getLayoutName());
    }

    @Test
    public void testSetLayoutName() {
        // the test layout editor setUp created is named this
        Assert.assertEquals("getLayoutName", "Layout Editor Test Layout", e.getLayoutName());
        // set to a known (different) value
        e.setLayoutName("foo");
        Assert.assertEquals("getLayoutName after set", "foo", e.getLayoutName());
    }

    @Test
    public void testGetShowHelpBar() {

        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(true);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertTrue("getShowHelpBar", e.getShowHelpBar());
        });
        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(false);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertFalse("getShowHelpBar", e.getShowHelpBar());
        });
    }

    @Test
    public void testSetShowHelpBar() {

        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(false);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertFalse("getShowHelpBar after set", e.getShowHelpBar());
        });
        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(true);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertTrue("getShowHelpBar", e.getShowHelpBar());
        });
        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(false);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertFalse("getShowHelpBar", e.getShowHelpBar());
        });
        ThreadingUtil.runOnGUI(() -> {
            e.setShowHelpBar(true);
        });
        ThreadingUtil.runOnGUI(() -> {
            Assert.assertTrue("getShowHelpBar", e.getShowHelpBar());
        });
    }

    @Test
    public void testGetDrawGrid() {
        // default to true
        Assert.assertTrue("getDrawGrid", e.getDrawGrid());
    }

    @Test
    public void testSetDrawGrid() {
        // default to false, so set to true.
        e.setDrawGrid(true);
        Assert.assertTrue("getDrawGrid after set", e.getDrawGrid());
    }

    @Test
    public void testGetSnapOnAdd() {
        // default to false
        Assert.assertFalse("getSnapOnAdd", e.getSnapOnAdd());
    }

    @Test
    public void testSetSnapOnAdd() {
        // default to false, so set to true.
        e.setSnapOnAdd(true);
        Assert.assertTrue("getSnapOnAdd after set", e.getSnapOnAdd());
    }

    @Test
    public void testGetSnapOnMove() {
        // default to false
        Assert.assertFalse("getSnapOnMove", e.getSnapOnMove());
    }

    @Test
    public void testSetSnapOnMove() {
        // default to false, so set to true.
        e.setSnapOnMove(true);
        Assert.assertTrue("getSnapOnMove after set", e.getSnapOnMove());
    }

    @Test
    public void testGetAntialiasingOn() {
        // default to false
        Assert.assertFalse("getAntialiasingOn", e.getAntialiasingOn());
    }

    @Test
    public void testSetAntialiasingOn() {
        // default to false, so set to true.
        e.setAntialiasingOn(true);
        Assert.assertTrue("getAntialiasingOn after set", e.getAntialiasingOn());
    }

    @Test
    public void testGetTurnoutCircles() {
        // default to false
        Assert.assertFalse("getTurnoutCircles", e.getTurnoutCircles());
    }

    @Test
    public void testSetTurnoutCircles() {
        // default to false, so set to true.
        e.setTurnoutCircles(true);
        Assert.assertTrue("getSetTurnoutCircles after set", e.getTurnoutCircles());
    }

    @Test
    public void testGetTooltipsNotEdit() {
        // default to false
        Assert.assertFalse("getTooltipsNotEdit", e.getTooltipsNotEdit());
    }

    @Test
    public void testSetTooltipsNotEdit() {
        // default to false, so set to true.
        e.setTooltipsNotEdit(true);
        Assert.assertTrue("getTooltipsNotEdit after set", e.getTooltipsNotEdit());
    }

    @Test
    public void testGetTooltipsInEdit() {
        // default to true
        Assert.assertTrue("getTooltipsInEdit", e.getTooltipsInEdit());
    }

    @Test
    public void testSetTooltipsInEdit() {
        // default to true, so set to false.
        e.setTooltipsInEdit(false);
        Assert.assertFalse("getTooltipsInEdit after set", e.getTooltipsInEdit());
    }

    @Test
    public void testGetAutoBlockAssignment() {
        // default to false
        Assert.assertFalse("getAutoBlockAssignment", e.getAutoBlockAssignment());
    }

    @Test
    public void testSetAutoBlockAssignment() {
        // default to false, so set to true.
        e.setAutoBlockAssignment(true);
        Assert.assertTrue("getAutoBlockAssignment after set", e.getAutoBlockAssignment());
    }

    @Test
    public void testGetTurnoutBX() {
        // defaults to 20.
        Assert.assertEquals("getTurnoutBX", 20.0, e.getTurnoutBX(), 0.0);
    }

    @Test
    public void testSetTurnoutBX() {
        // set to known value
        e.setTurnoutBX(2.0);
        Assert.assertEquals("getTurnoutBX after set ", 2.0, e.getTurnoutBX(), 0.0);
    }

    @Test
    public void testGetTurnoutCX() {
        // defaults to 20.
        Assert.assertEquals("getTurnoutCX", 20.0, e.getTurnoutCX(), 0.0);
    }

    @Test
    public void testSetTurnoutCX() {
        // set to known value
        e.setTurnoutCX(2.0);
        Assert.assertEquals("getTurnoutCX after set ", 2.0, e.getTurnoutCX(), 0.0);
    }

    @Test
    public void testGetTurnoutWid() {
        // defaults to 10.
        Assert.assertEquals("getTurnoutWid", 10.0, e.getTurnoutWid(), 0.0);
    }

    @Test
    public void testSetTurnoutWid() {
        // set to known value
        e.setTurnoutWid(2.0);
        Assert.assertEquals("getTurnoutWid after set ", 2.0, e.getTurnoutWid(), 0.0);
    }

    @Test
    public void testGetXOverLong() {
        // defaults to 30.
        Assert.assertEquals("getXOverLong", 30.0, e.getXOverLong(), 0.0);
    }

    @Test
    public void testSetXOverLong() {
        // set to known value
        e.setXOverLong(2.0);
        Assert.assertEquals("getXOverLong after set ", 2.0, e.getXOverLong(), 0.0);
    }

    @Test
    public void testGetXOverHWid() {
        // defaults to 10.
        Assert.assertEquals("getXOverHWid", 10.0, e.getXOverHWid(), 0.0);
    }

    @Test
    public void testSetXOverHWid() {
        // set to known value
        e.setXOverHWid(2.0);
        Assert.assertEquals("getXOverWid after set ", 2.0, e.getXOverHWid(), 0.0);
    }

    @Test
    public void testGetXOverShort() {
        // defaults to 10.
        Assert.assertEquals("getXOverShort", 10.0, e.getXOverShort(), 0.0);
    }

    @Test
    public void testSetXOverShort() {
        // set to known value
        e.setXOverShort(2.0);
        Assert.assertEquals("getXOverShort after set ", 2.0, e.getXOverShort(), 0.0);
    }

    @Test
    public void testResetTurnoutSizes() {
        // set all dimensions to known value
        e.setTurnoutBX(2.0);
        e.setTurnoutCX(2.0);
        e.setTurnoutWid(2.0);
        e.setXOverLong(2.0);
        e.setXOverHWid(2.0);
        e.setXOverShort(2.0);

        // reset - uses reflection to get a private method.
        java.lang.reflect.Method resetTurnoutSize = null;
        try {
            resetTurnoutSize = e.getClass().getDeclaredMethod("resetTurnoutSize");
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method resetTurnoutSize in LayoutEditor class.");
        }
        // override the default permissions.
        Assert.assertNotNull(resetTurnoutSize);
        resetTurnoutSize.setAccessible(true);
        try {
            resetTurnoutSize.invoke(e);
        } catch (java.lang.IllegalAccessException iae) {
            Assert.fail("Could not access method resetTurnoutSize in LayoutEditor class.");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Assertions.fail("resetTurnoutSize execution failed reason: ", cause);
        }

        // then check for the default values.
        Assert.assertEquals("getTurnoutBX", 20.0, e.getTurnoutBX(), 0.0);
        Assert.assertEquals("getTurnoutCX", 20.0, e.getTurnoutBX(), 0.0);
        Assert.assertEquals("getTurnoutWid", 20.0, e.getTurnoutBX(), 0.0);
        Assert.assertEquals("getXOverLong", 30.0, e.getXOverLong(), 0.0);
        Assert.assertEquals("getXOverHWid", 30.0, e.getXOverLong(), 0.0);
        Assert.assertEquals("getXOverShort", 30.0, e.getXOverLong(), 0.0);
        // and reset also sets the dirty bit.
        Assert.assertTrue("isDirty after resetTurnoutSize", e.isDirty());
    }

    @Test
    public void testGetDirectTurnoutControl() {
        // default to false
        Assert.assertFalse("getDirectTurnoutControl", e.getDirectTurnoutControl());
    }

    @Test
    public void testSetDirectTurnoutControl() {
        // default to false, so set to true.
        e.setDirectTurnoutControl(true);
        Assert.assertTrue("getDirectTurnoutControl after set", e.getDirectTurnoutControl());
    }

    @Test
    public void testSetDirectTurnoutControlOff() {
        e.setDirectTurnoutControl(false);
        Assert.assertFalse("getDirectTurnoutControl after set", e.getDirectTurnoutControl());
    }

    @Test
    public void testIsEditableDefault() {
        // default to true
        Assert.assertTrue("isEditable default true", e.isEditable());
    }

    @Test
    public void testSetAllEditableFalse() {
        e.setAllEditable(false);
        Assert.assertFalse("isEditable after setAllEditable(false)", e.isEditable());
    }

    @Test
    public void testSetAllEditableTrue() {
        e.setAllEditable(true);
        Assert.assertTrue("isEditable after setAllEditable(true)", e.isEditable());
    }

    @Test
    public void testGetHighlightSelectedBlockDefault() {
        // default to false
        Assert.assertFalse("le.getHighlightSelectedBlock default false", e.getHighlightSelectedBlock());
    }

    @Test
    @Disabled("unreliable on CI servers")
    public void testSetHighlightSelectedBlockTrue() {
        e.setHighlightSelectedBlock(true);
        // setHighlightSelectedBlock performs some GUI actions, so give
        // the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertTrue("le.getHighlightSelectedBlock after setHighlightSelectedBlock(true)", e.getHighlightSelectedBlock());
    }

    @Test
    @Disabled("unreliable on CI servers")
    public void testSetHighlightSelectedBlockFalse() {
        e.setHighlightSelectedBlock(false);
        // setHighlightSelectedBlock performs some GUI actions, so give
        // the AWT queue some time to clear.
        new QueueTool().waitEmpty(100);
        Assert.assertFalse("le.getHighlightSelectedBlock after setHighlightSelectedBlock(false)", e.getHighlightSelectedBlock());
    }

    @Test
    public void checkOptionsMenuExists() {
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));
        Assert.assertNotNull("Options Menu Exists", jmo);
        Assert.assertEquals("Menu Item Count", 20, jmo.getItemCount());
    }

    @Test
    public void checkToolsMenuExists() {
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuTools"));
        Assert.assertNotNull("Tools Menu Exists", jmo);
        Assert.assertEquals("Tools Menu Item Count", 20, jmo.getItemCount());
    }

    @Test
    public void checkZoomMenuExists() {
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuZoom"));
        Assert.assertNotNull("Zoom Menu Exists", jmo);
        Assert.assertEquals("Menu Item Count", 16, jmo.getItemCount());
    }

    @Test
    public void checkMarkerMenuExists() {
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuMarker"));
        Assert.assertNotNull("Marker Menu Exists", jmo);
        Assert.assertEquals("Menu Item Count", 3, jmo.getItemCount());
    }

    @Test
    public void checkDispatcherMenuExists() {
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuDispatcher"));
        Assert.assertNotNull("Dispatcher Menu Exists", jmo);
        Assert.assertEquals("Menu Item Count", 2, jmo.getItemCount());
    }

    @Test
    @Disabled("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testToolBarPositionLeft() {
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));

        //switch to Left
        jmo.pushMenuNoBlock(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideLeft"), "/");

        new EventTool().waitNoEvent(200);

        //back to Top
        jmo.pushMenu(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideTop"), "/");
    }

    @Test
    @Disabled("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testToolBarPositionBottom() {
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));

        //switch to Bottom
        jmo.pushMenuNoBlock(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideBottom"), "/");

        new EventTool().waitNoEvent(200);

        //back to Top
        jmo.pushMenu(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideTop"), "/");
    }

    @Test
    @Disabled("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testToolBarPositionRight() {
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));

        //switch to Right
        jmo.pushMenuNoBlock(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideRight"), "/");

        new EventTool().waitNoEvent(200);

        //back to Top
        jmo.pushMenu(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideTop"), "/");
    }

    @Test
    @Disabled("Fails on AppVeyor, macOS and Windows 12/20/2019")
    public void testToolBarPositionFloat() {
        Assertions.assertNotNull(jfo);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));

        //switch to Float
        jmo.pushMenuNoBlock(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideFloat"), "/");

        // bring this window back to the front...
        jfo.activate();

        //back to Top
        jmo.pushMenu(Bundle.getMessage("MenuOptions") + "/"
                + Bundle.getMessage("ToolBar") + "/"
                + Bundle.getMessage("ToolBarSide") + "/"
                + Bundle.getMessage("ToolBarSideTop"), "/");
    }

    @Test
    public void testGetLEAuxTools() {
        LayoutEditorAuxTools t = e.getLEAuxTools();
        Assert.assertNotNull("tools exist", t);
    }

    @Test
    public void testScrollViewPort() {
        javax.swing.JScrollPane scrollPane = e.getPanelScrollPane();
        java.awt.Rectangle scrollBounds = scrollPane.getViewportBorderBounds();
        int x = (int) scrollBounds.getX();
        int y = (int) scrollBounds.getY();
        int w = (int) scrollBounds.getWidth();
        int h = (int) scrollBounds.getHeight();
        // scrollBounds values are platform and OS dependent so specific values cannot be determined.
        Assert.assertTrue("scroll bound x", x > 0);
        Assert.assertTrue("scroll bound y", y > 0);
        Assert.assertTrue("scroll bound w", w > 0);
        Assert.assertTrue("scroll bound h", h > 0);
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorTest.class.getName());
}
