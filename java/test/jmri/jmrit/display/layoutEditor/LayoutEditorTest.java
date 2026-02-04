package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Toolkit;
import java.io.File;

import jmri.*;
import jmri.jmrit.display.*;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;
import jmri.util.ThreadingUtil;

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
        assertNotNull(jfo);
        jfo.closeFrameWithConfirmations(); // close layoutEditor created in setUp
        LayoutEditor stringCtorEditor = new LayoutEditor("Layout Editor String Constructor");
        assertNotNull( stringCtorEditor, "exists");
        JUnitUtil.dispose(stringCtorEditor);
    }

    @Test
    public void testDefaultCtor() {
        assertNotNull(jfo);
        jfo.closeFrameWithConfirmations(); // close layoutEditor created in setUp
        LayoutEditor defaultCtorEditor = new LayoutEditor(); // create layout editor
        assertNotNull( defaultCtorEditor, "exists");
        JUnitUtil.dispose(defaultCtorEditor);
    }

    @Test
    @Disabled("Test fails to find and close dialog on Jenkins")
    public void testSavePanel() {

        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuFile"));

        //delete this file so we won't get the "<xxx> exists... do you want to replace?" dialog.
        assertTrue(new File("temp/Layout Editor Test Layout.xml").delete());

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
        assertTrue(new File("temp/Layout Editor Test Layout.xml").delete());
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
        assertNotNull( f, "exists");
    }

    @Test
    @Override
    @Disabled("failing to set size on appveyor")
    public void testSetSize() {
        e.setSize(100, 100);
        java.awt.Dimension d = e.getSize();

        // the java.awt.Dimension stores the values as floating point
        // numbers, but setSize expects integer parameters.
        assertEquals( 100.0, d.getWidth(), 0.0, "Width Set");
        assertEquals( 100.0, d.getHeight(), 0.0, "Height Set");
    }

    @Test
    @Disabled("Failing to set second zoom")
    public void testGetSetZoom() {
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((m) -> {
            m.setSaveAllowed(false); // prevent attempts to save while zooming in rest of test
        });
        assertEquals( 1.0, e.getZoom(), 0.0, "Get initial Zoom");

        // note: Layout Editor won't allow zooms above 8.0.
        e.setZoom(10.0);
        assertEquals( 8.0, e.getZoom(), 0.0, "Get Zoom after set above max");
        e.setZoom(3.33);
        assertEquals( 3.33, e.getZoom(), 0.0, "Get Zoom After Set to 3.33");
    }

    @Test
    public void testGetOpenDispatcherOnLoad() {
        // defaults to false.
        assertFalse( e.getOpenDispatcherOnLoad(), "getOpenDispatcherOnLoad");
    }

    @Test
    public void testSetOpenDispatcherOnLoad() {
        // defaults to false, so set to true.
        e.setOpenDispatcherOnLoad(true);
        assertTrue( e.getOpenDispatcherOnLoad(), "setOpenDispatcherOnLoad after set");
    }

    @Test
    public void testIsDirty() {
        // defaults to false.
        assertFalse( e.isDirty(), "isDirty");
    }

    @Test
    public void testSetDirty() {
        // defaults to false, setDirty() sets it to true.
        e.setDirty();
        assertTrue( e.isDirty(), "isDirty after set");
    }

    @Test
    public void testSetDirtyWithParameter() {
        // defaults to false, so set it to true.
        e.setDirty(true);
        assertTrue( e.isDirty(), "isDirty after set");
    }

    @Test
    public void testResetDirty() {
        // defaults to false, so set it to true.
        e.setDirty(true);
        // then call resetDirty, which sets it back to false.
        e.resetDirty();
        assertFalse( e.isDirty(), "isDirty after reset");
    }

    @Test
    public void testIsAnimating() {
        // default to true
        assertTrue( e.isAnimating(), "isAnimating");
    }

    @Test
    public void testSetTurnoutAnimating() {
        // default to true, so set to false.
        e.setTurnoutAnimation(false);
        assertFalse( e.isAnimating(), "isAnimating after set");
    }

    @Test
    public void testGetLayoutWidth() {
        // defaults to 0
        assertEquals( 0, e.gContext.getLayoutWidth(), "layout width");
    }

    @Test
    public void testGetLayoutHeight() {
        // defaults to 0
        assertEquals( 0, e.gContext.getLayoutHeight(), "layout height");
    }

    @Test
    public void testGetWindowWidth() {
        // defaults to screen width - 20
        int w = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 20);
        assertEquals( w, e.gContext.getWindowWidth(), "window width");
    }

    @Test
    public void testGetWindowHeight() {
        // defaults to screen height - 120
        int h = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 120);
        assertEquals( h, e.gContext.getWindowHeight(), "window height");
    }

    @Test
    public void testGetUpperLeftX() {
        // defaults to 0
        assertEquals( 0, e.gContext.getUpperLeftX(), "upper left X");
    }

    @Test
    public void testGetUpperLeftY() {
        // defaults to 0
        assertEquals( 0, e.gContext.getUpperLeftY(), "upper left Y");
    }

    @Test
    public void testSetLayoutDimensions() {
        boolean complete = ThreadingUtil.runOnGUIwithReturn(() -> {
            // set the panel dimensions to known values
            e.setLayoutDimensions(100, 100, 100, 100, 100, 100);
            assertEquals( 100, e.gContext.getLayoutWidth(), "layout width after set");
            assertEquals( 100, e.gContext.getLayoutHeight(), "layout height after set");
            assertEquals( 100, e.gContext.getWindowWidth(), "window width after set");
            assertEquals( 100, e.gContext.getWindowHeight(), "window height after set");
            assertEquals( 100, e.gContext.getUpperLeftX(), "upper left X after set");
            assertEquals( 100, e.gContext.getUpperLeftX(), "upper left Y after set");
            return true;
        });
        assertTrue(complete);
    }

    @Test
    public void testSetGrideSize() {
        assertEquals( 100, e.gContext.setGridSize(100), "grid size after set");
    }

    @Test
    public void testGetGrideSize() {
        // defaults to 10.
        assertEquals( 10, e.gContext.getGridSize(), "grid size");
    }

    @Test
    public void testGetMainlineTrackWidth() {
        // defaults to 4.
        assertEquals( 4, e.gContext.getMainlineTrackWidth(), "mainline track width");
    }

    @Test
    public void testSetMainlineTrackWidth() {
        // set to known value
        e.gContext.setMainlineTrackWidth(10);
        assertEquals( 10, e.gContext.getMainlineTrackWidth(), "mainline track width after set");
    }

    @Test
    public void testGetSidelineTrackWidth() {
        // defaults to 2.
        assertEquals( 2, e.gContext.getSidelineTrackWidth(), "side track width");
    }

    @Test
    public void testSetSideTrackWidth() {
        // set to known value
        e.gContext.setSidelineTrackWidth(10);
        assertEquals( 10, e.gContext.getSidelineTrackWidth(), "Side track width after set");
    }

    @Test
    public void testGetXScale() {
        // defaults to 1.
        assertEquals( 1.0, e.gContext.getXScale(), 0.0, "XScale");
    }

    @Test
    public void testSetXScale() {
        // set to known value
        e.gContext.setXScale(2.0);
        assertEquals( 2.0, e.gContext.getXScale(), 0.0, "XScale after set");
    }

    @Test
    public void testGetYScale() {
        // defaults to 1.
        assertEquals( 1.0, e.gContext.getYScale(), 0.0, "YScale");
    }

    @Test
    public void testSetYScale() {
        // set to known value
        e.gContext.setYScale(2.0);
        assertEquals( 2.0, e.gContext.getYScale(), 0.0, "YScale after set ");
    }

    @Test
    public void testGetDefaultTrackColor() {
        assertEquals( ColorUtil.ColorDarkGray, e.getDefaultTrackColor(), "Default Track Color");
    }

    @Test
    public void testSetDefaultTrackColor() {
        e.setDefaultTrackColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        assertEquals( ColorUtil.ColorPink, e.getDefaultTrackColor(), "Default Track Color after Set");
    }

    @Test
    public void testGetDefaultOccupiedTrackColor() {
        assertEquals( "red", e.getDefaultOccupiedTrackColor(), "Default Occupied Track Color");
    }

    @Test
    public void testSetDefaultOccupiedTrackColor() {
        e.setDefaultOccupiedTrackColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        assertEquals( ColorUtil.ColorPink, e.getDefaultOccupiedTrackColor(), "Default Occupied Track Color after Set");
    }

    @Test
    public void testGetDefaultAlternativeTrackColor() {
        assertEquals( ColorUtil.ColorWhite, e.getDefaultAlternativeTrackColor(), "Default Alternative Track Color");
    }

    @Test
    public void testSetDefaultAlternativeTrackColor() {
        e.setDefaultAlternativeTrackColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        assertEquals( ColorUtil.ColorPink, e.getDefaultAlternativeTrackColor(), "Default Alternative Track Color after Set");
    }

    @Test
    public void testSetAllTracksToDefaultColors() {

        LayoutBlock layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock("ILB999", "Test Block");
        assertNotNull( layoutBlock, "layoutBlock created");

        assertEquals( e.getDefaultTrackColorColor(), layoutBlock.getBlockTrackColor(), "BlockTrackColor default");
        layoutBlock.setBlockTrackColor(Color.pink);
        assertEquals( Color.pink, layoutBlock.getBlockTrackColor(), "BlockTrackColor set to pink");

        assertEquals( e.getDefaultOccupiedTrackColorColor(), layoutBlock.getBlockOccupiedColor(), "BlockOccupiedColor default");
        layoutBlock.setBlockOccupiedColor(Color.pink);
        assertEquals( Color.pink, layoutBlock.getBlockOccupiedColor(), "BlockOccupiedColor set to pink");

        assertEquals( e.getDefaultAlternativeTrackColorColor(), layoutBlock.getBlockExtraColor(), "BlockExtraColor default");
        layoutBlock.setBlockExtraColor(Color.pink);
        assertEquals( Color.pink, layoutBlock.getBlockExtraColor(), "BlockExtraColor set to pink");

        int changed = e.setAllTracksToDefaultColors();
        assertEquals( 1, changed, "setAllTracksToDefaultColors changed one block");

        assertEquals( e.getDefaultTrackColorColor(), layoutBlock.getBlockTrackColor(),
                "BlockTrackColor back to default");
        assertEquals( e.getDefaultOccupiedTrackColorColor(), layoutBlock.getBlockOccupiedColor(),
                "BlockOccupiedColor back to default");
        assertEquals( e.getDefaultAlternativeTrackColorColor(), layoutBlock.getBlockExtraColor(),
                "BlockExtraColor back to default");
    }

    @Test
    public void testGetDefaultTextColor() {
        assertEquals( ColorUtil.ColorBlack, e.getDefaultTextColor(), "Default Text Color");
    }

    @Test
    public void testSetDefaultTextColor() {
        e.setDefaultTextColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        assertEquals( ColorUtil.ColorPink, e.getDefaultTextColor(), "Default Text Color after Set");
    }

    @Test
    public void testGetTurnoutCircleColor() {
        assertEquals( ColorUtil.ColorBlack, e.getTurnoutCircleColor(), "Turnout Circle Color");
    }

    @Test
    public void testSetTurnoutCircleColor() {
        e.setTurnoutCircleColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        assertEquals( ColorUtil.ColorPink, e.getTurnoutCircleColor(), "Turnout Circle after Set");
    }

    @Test
    public void testGetTurnoutCircleThrownColor() {
        assertEquals( ColorUtil.ColorBlack, e.getTurnoutCircleThrownColor(), "Turnout Circle Thrown Color");
    }

    @Test
    public void testSetTurnoutCircleThrownColor() {
        e.setTurnoutCircleThrownColor(ColorUtil.stringToColor(ColorUtil.ColorPink));
        assertEquals( ColorUtil.ColorPink, e.getTurnoutCircleThrownColor(), "Turnout Circle after Set");
    }

    @Test
    public void testIsTurnoutFillControlCircles() {
        // default to false
        assertFalse( e.isTurnoutFillControlCircles(), "isTurnoutFillControlCircles");
    }

    @Test
    public void testSetTurnoutFillControlCircles() {
        // default to false, so set to true.
        e.setTurnoutFillControlCircles(true);
        assertTrue( e.isTurnoutFillControlCircles(), "isTurnoutFillControlCircles after set true");
        // set back to default (false) and confirm new value
        e.setTurnoutFillControlCircles(false);
        assertFalse( e.isTurnoutFillControlCircles(), "isTurnoutFillControlCircles after set false");
    }

    @Test
    public void testGetTurnoutCircleSize() {
        // defaults to 4.
        assertEquals( 4, e.getTurnoutCircleSize(), "turnout circle size");
    }

    @Test
    public void testSetTurnoutCircleSize() {
        e.setTurnoutCircleSize(11);
        assertEquals( 11, e.getTurnoutCircleSize(), "turnout circle size after set");
    }

    @Test
    public void testGetTurnoutDrawUnselectedLeg() {
        // default to true
        assertTrue( e.isTurnoutDrawUnselectedLeg(), "getTurnoutDrawUnselectedLeg");
    }

    @Test
    public void testSetTurnoutDrawUnselectedLeg() {
        // default to true, so set to false.
        e.setTurnoutDrawUnselectedLeg(false);
        assertFalse( e.isTurnoutDrawUnselectedLeg(), "getTurnoutDrawUnselectedLeg after set");
    }

    @Test
    public void testGetLayoutName() {
        e.dispose(); // remove existing instance
        e = new LayoutEditor(); // create new instance to test the default name
        jfo = new EditorFrameOperator(e);
        // default is "My Layout"
        assertEquals( "My Layout", e.getLayoutName(), "getLayoutName");
    }

    @Test
    public void testSetLayoutName() {
        // the test layout editor setUp created is named this
        assertEquals( "Layout Editor Test Layout", e.getLayoutName(), "getLayoutName");
        // set to a known (different) value
        e.setLayoutName("foo");
        assertEquals( "foo", e.getLayoutName(), "getLayoutName after set");
    }

    @Test
    public void testGetShowHelpBar() {

        boolean complete = ThreadingUtil.runOnGUIwithReturn(() -> {
            e.setShowHelpBar(true);
            assertTrue( e.getShowHelpBar(), "getShowHelpBar");

            e.setShowHelpBar(false);
            assertFalse( e.getShowHelpBar(), "getShowHelpBar");

            return true;
        });
        assertTrue(complete);
    }

    @Test
    public void testSetShowHelpBar() {

        boolean complete = ThreadingUtil.runOnGUIwithReturn(() -> {
            e.setShowHelpBar(false);
            assertFalse( e.getShowHelpBar(), "getShowHelpBar after set");

            e.setShowHelpBar(true);
            assertTrue( e.getShowHelpBar(), "getShowHelpBar");

            e.setShowHelpBar(false);
            assertFalse( e.getShowHelpBar(), "getShowHelpBar");

            e.setShowHelpBar(true);
            assertTrue( e.getShowHelpBar(), "getShowHelpBar");

            return true;
        });
        assertTrue(complete);
    }

    @Test
    public void testGetDrawGrid() {
        // default to true
        assertTrue( e.getDrawGrid(), "getDrawGrid");
    }

    @Test
    public void testSetDrawGrid() {
        // default to false, so set to true.
        e.setDrawGrid(true);
        assertTrue( e.getDrawGrid(), "getDrawGrid after set");
    }

    @Test
    public void testGetSnapOnAdd() {
        // default to false
        assertFalse( e.getSnapOnAdd(), "getSnapOnAdd");
    }

    @Test
    public void testSetSnapOnAdd() {
        // default to false, so set to true.
        e.setSnapOnAdd(true);
        assertTrue( e.getSnapOnAdd(), "getSnapOnAdd after set");
    }

    @Test
    public void testGetSnapOnMove() {
        // default to false
        assertFalse( e.getSnapOnMove(), "getSnapOnMove");
    }

    @Test
    public void testSetSnapOnMove() {
        // default to false, so set to true.
        e.setSnapOnMove(true);
        assertTrue( e.getSnapOnMove(), "getSnapOnMove after set");
    }

    @Test
    public void testGetAntialiasingOn() {
        // default to false
        assertFalse( e.getAntialiasingOn(), "getAntialiasingOn");
    }

    @Test
    public void testSetAntialiasingOn() {
        // default to false, so set to true.
        e.setAntialiasingOn(true);
        assertTrue( e.getAntialiasingOn(), "getAntialiasingOn after set");
    }

    @Test
    public void testGetTurnoutCircles() {
        // default to false
        assertFalse( e.getTurnoutCircles(), "getTurnoutCircles");
    }

    @Test
    public void testSetTurnoutCircles() {
        // default to false, so set to true.
        e.setTurnoutCircles(true);
        assertTrue( e.getTurnoutCircles(), "getSetTurnoutCircles after set");
    }

    @Test
    public void testGetTooltipsNotEdit() {
        // default to false
        assertFalse( e.getTooltipsNotEdit(), "getTooltipsNotEdit");
    }

    @Test
    public void testSetTooltipsNotEdit() {
        // default to false, so set to true.
        e.setTooltipsNotEdit(true);
        assertTrue( e.getTooltipsNotEdit(), "getTooltipsNotEdit after set");
    }

    @Test
    public void testGetTooltipsInEdit() {
        // default to true
        assertTrue( e.getTooltipsInEdit(), "getTooltipsInEdit");
    }

    @Test
    public void testSetTooltipsInEdit() {
        // default to true, so set to false.
        e.setTooltipsInEdit(false);
        assertFalse( e.getTooltipsInEdit(), "getTooltipsInEdit after set");
    }

    @Test
    public void testGetAutoBlockAssignment() {
        // default to false
        assertFalse( e.getAutoBlockAssignment(), "getAutoBlockAssignment");
    }

    @Test
    public void testSetAutoBlockAssignment() {
        // default to false, so set to true.
        e.setAutoBlockAssignment(true);
        assertTrue( e.getAutoBlockAssignment(), "getAutoBlockAssignment after set");
    }

    @Test
    public void testGetTurnoutBX() {
        // defaults to 20.
        assertEquals( 20.0, e.getTurnoutBX(), 0.0, "getTurnoutBX");
    }

    @Test
    public void testSetTurnoutBX() {
        // set to known value
        e.setTurnoutBX(2.0);
        assertEquals( 2.0, e.getTurnoutBX(), 0.0, "getTurnoutBX after set ");
    }

    @Test
    public void testGetTurnoutCX() {
        // defaults to 20.
        assertEquals( 20.0, e.getTurnoutCX(), 0.0, "getTurnoutCX");
    }

    @Test
    public void testSetTurnoutCX() {
        // set to known value
        e.setTurnoutCX(2.0);
        assertEquals( 2.0, e.getTurnoutCX(), 0.0, "getTurnoutCX after set ");
    }

    @Test
    public void testGetTurnoutWid() {
        // defaults to 10.
        assertEquals( 10.0, e.getTurnoutWid(), 0.0, "getTurnoutWid");
    }

    @Test
    public void testSetTurnoutWid() {
        // set to known value
        e.setTurnoutWid(2.0);
        assertEquals( 2.0, e.getTurnoutWid(), 0.0, "getTurnoutWid after set");
    }

    @Test
    public void testGetXOverLong() {
        // defaults to 30.
        assertEquals( 30.0, e.getXOverLong(), 0.0, "getXOverLong");
    }

    @Test
    public void testSetXOverLong() {
        // set to known value
        e.setXOverLong(2.0);
        assertEquals( 2.0, e.getXOverLong(), 0.0, "getXOverLong after set ");
    }

    @Test
    public void testGetXOverHWid() {
        // defaults to 10.
        assertEquals( 10.0, e.getXOverHWid(), 0.0, "getXOverHWid");
    }

    @Test
    public void testSetXOverHWid() {
        // set to known value
        e.setXOverHWid(2.0);
        assertEquals( 2.0, e.getXOverHWid(), 0.0, "getXOverWid after set ");
    }

    @Test
    public void testGetXOverShort() {
        // defaults to 10.
        assertEquals( 10.0, e.getXOverShort(), 0.0, "getXOverShort");
    }

    @Test
    public void testSetXOverShort() {
        // set to known value
        e.setXOverShort(2.0);
        assertEquals( 2.0, e.getXOverShort(), 0.0, "getXOverShort after set ");
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
        java.lang.reflect.Method resetTurnoutSize = assertDoesNotThrow( () ->
            e.getClass().getDeclaredMethod("resetTurnoutSize"),
                "Could not find method resetTurnoutSize in LayoutEditor class.");

        // override the default permissions.
        assertNotNull(resetTurnoutSize);
        resetTurnoutSize.setAccessible(true);
        assertDoesNotThrow( () ->
            resetTurnoutSize.invoke(e),
                "resetTurnoutSize execution failed");

        // then check for the default values.
        assertEquals( 20.0, e.getTurnoutBX(), 0.0, "getTurnoutBX");
        assertEquals( 20.0, e.getTurnoutBX(), 0.0, "getTurnoutCX");
        assertEquals( 20.0, e.getTurnoutBX(), 0.0, "getTurnoutWid");
        assertEquals( 30.0, e.getXOverLong(), 0.0, "getXOverLong");
        assertEquals( 30.0, e.getXOverLong(), 0.0, "getXOverHWid");
        assertEquals( 30.0, e.getXOverLong(), 0.0, "getXOverShort");
        // and reset also sets the dirty bit.
        assertTrue( e.isDirty(), "isDirty after resetTurnoutSize");
    }

    @Test
    public void testGetDirectTurnoutControl() {
        // default to false
        assertFalse( e.getDirectTurnoutControl(), "getDirectTurnoutControl");
    }

    @Test
    public void testSetDirectTurnoutControl() {
        // default to false, so set to true.
        e.setDirectTurnoutControl(true);
        assertTrue( e.getDirectTurnoutControl(), "getDirectTurnoutControl after set");
    }

    @Test
    public void testSetDirectTurnoutControlOff() {
        e.setDirectTurnoutControl(false);
        assertFalse( e.getDirectTurnoutControl(), "getDirectTurnoutControl after set");
    }

    @Test
    public void testIsEditableDefault() {
        // default to true
        assertTrue( e.isEditable(), "isEditable default true");
    }

    @Test
    public void testSetAllEditableFalse() {
        e.setAllEditable(false);
        assertFalse( e.isEditable(), "isEditable after setAllEditable(false)");
    }

    @Test
    public void testSetAllEditableTrue() {
        e.setAllEditable(true);
        assertTrue( e.isEditable(), "isEditable after setAllEditable(true)");
    }

    @Test
    public void testGetHighlightSelectedBlockDefault() {
        // default to false
        assertFalse( e.getHighlightSelectedBlock(), "le.getHighlightSelectedBlock default false");
    }

    @Test
    @Disabled("unreliable on CI servers")
    public void testSetHighlightSelectedBlockTrue() {
        e.setHighlightSelectedBlock(true);
        // setHighlightSelectedBlock performs some GUI actions, so give
        // the AWT queue some time to clear.
        new QueueTool().waitEmpty();
        assertTrue( e.getHighlightSelectedBlock(),
                "le.getHighlightSelectedBlock after setHighlightSelectedBlock(true)");
    }

    @Test
    @Disabled("unreliable on CI servers")
    public void testSetHighlightSelectedBlockFalse() {
        e.setHighlightSelectedBlock(false);
        // setHighlightSelectedBlock performs some GUI actions, so give
        // the AWT queue some time to clear.
        new QueueTool().waitEmpty();
        assertFalse( e.getHighlightSelectedBlock(),
                "le.getHighlightSelectedBlock after setHighlightSelectedBlock(false)");
    }

    @Test
    public void checkOptionsMenuExists() {
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));
        assertNotNull( jmo, "Options Menu Exists");
        assertEquals( 20, jmo.getItemCount(), "Menu Item Count");
    }

    @Test
    public void checkToolsMenuExists() {
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuTools"));
        assertNotNull( jmo, "Tools Menu Exists");
        assertEquals( 20, jmo.getItemCount(), "Tools Menu Item Count");
    }

    @Test
    public void checkZoomMenuExists() {
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuZoom"));
        assertNotNull( jmo, "Zoom Menu Exists");
        assertEquals( 16, jmo.getItemCount(), "Menu Item Count");
    }

    @Test
    public void checkMarkerMenuExists() {
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuMarker"));
        assertNotNull( jmo, "Marker Menu Exists");
        assertEquals( 3, jmo.getItemCount(), "Menu Item Count");
    }

    @Test
    public void checkDispatcherMenuExists() {
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuDispatcher"));
        assertNotNull( jmo, "Dispatcher Menu Exists");
        assertEquals( 2, jmo.getItemCount(), "Menu Item Count");
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
        assertNotNull(jfo);
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
        assertNotNull( t, "tools exist");
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
        assertTrue( x > 0, "scroll bound x");
        assertTrue( y > 0, "scroll bound y");
        assertTrue( w > 0, "scroll bound w");
        assertTrue( h > 0, "scroll bound h");
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorTest.class.getName());
}
