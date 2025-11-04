package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.Component;

import javax.swing.*;
import javax.annotation.*;

import jmri.*;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;
import jmri.util.junit.annotations.ToDo;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.operators.Operator.StringComparator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test simple functioning of LayoutTrackEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LayoutTrackEditorTest {

    @ToDo("Implementing subclasses should use own instance on this method")
    @Test
    public void testHasNxSensorPairsNull() {
        LayoutTrackEditor layoutTrackEditor = new LayoutTrackEditor(layoutEditor) { // core of abstract class
            @Override
            public void editLayoutTrack(@Nonnull LayoutTrackView layoutTrack) {}
        };

        assertThat(layoutTrackEditor.hasNxSensorPairs(null)).withFailMessage("null block NxSensorPairs").isFalse();
    }

    @ToDo("Implementing subclasses should use own instance on this method")
    @Test
    public void testHasNxSensorPairsDisconnectedBlock() {
        LayoutTrackEditor layoutTrackEditor = new LayoutTrackEditor(layoutEditor) { // core of abstract class
            @Override
            public void editLayoutTrack(@Nonnull LayoutTrackView layoutTrack) {}
        };

        LayoutBlock b = new LayoutBlock("test", "test");
        assertThat(layoutTrackEditor.hasNxSensorPairs(b)).withFailMessage("disconnected block NxSensorPairs").isFalse();
    }

    @ToDo("Implementing subclasses should use own instance on this method")
    @Test
    public void testShowSensorMessage() {
        LayoutTrackEditor layoutTrackEditor = new LayoutTrackEditor(layoutEditor) { // core of abstract class
            @Override
            public void editLayoutTrack(@Nonnull LayoutTrackView layoutTrack) {}
        };

        layoutTrackEditor.sensorList.add("Test");
        assertThat(layoutTrackEditor.sensorList).isNotEmpty();

        layoutTrackEditor.showSensorMessage();
    }

    protected LayoutEditor layoutEditor = null;

    @BeforeEach
    @OverridingMethodsMustInvokeSuper  // invoke first
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();

        layoutEditor = new LayoutEditor();
        ThreadingUtil.runOnGUI( () -> layoutEditor.setVisible(true) );

    }

    @AfterEach
    @OverridingMethodsMustInvokeSuper  // invoke last
    public void tearDown()  {
        if (layoutEditor != null) {
            EditorFrameOperator efo = new EditorFrameOperator(layoutEditor);
            efo.closeFrameWithConfirmations();
        }
        layoutEditor = null;
        jmri.jmrit.display.EditorFrameOperator.clearEditorFrameOperatorThreads();
        
        
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    protected Turnout turnout0 = null;
    protected Turnout turnout1 = null;

    /*
     * This is used to find a component by matching against its tooltip
     */
    protected static class ToolTipComponentChooser implements ComponentChooser {

        private final String buttonTooltip;
        private final StringComparator comparator = Operator.getDefaultStringComparator();

        ToolTipComponentChooser(String buttonTooltip) {
            this.buttonTooltip = buttonTooltip;
        }

        @Override
        public boolean checkComponent(Component comp) {
            return comparator.equals(((JComponent) comp).getToolTipText(), buttonTooltip);
        }

        @Override
        public String getDescription() {
            return "Component with tooltip \"" + buttonTooltip + "\".";
        }
    }


    protected void createTurnouts() {
        turnout0 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT101");
        turnout0.setUserName("Turnout 101");
        turnout0.setCommandedState(Turnout.CLOSED);

        turnout1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT102");
        turnout1.setUserName("Turnout 102");
        turnout1.setCommandedState(Turnout.CLOSED);
    }

    protected void createBlocks() {
        Block block1 = InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        block1.setUserName("Blk 1");
        Block block2 = InstanceManager.getDefault(BlockManager.class).provideBlock("IB2");
        block2.setUserName("Blk 2");
    }


    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTrackEditorTest.class);
}
