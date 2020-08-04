package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.Component;
import java.awt.GraphicsEnvironment;

import javax.swing.*;
import javax.annotation.*;

import jmri.*;
import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.operators.Operator.StringComparator;

/**
 * Test simple functioning of LayoutTrackEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutTrackEditorTest {

    @Test
    public void testHasNxSensorPairsNull() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutTrackEditor layoutTrackEditor = new LayoutTrackEditor(null) { // core of abstract class
            public void editLayoutTrack(@Nonnull LayoutTrack layoutTrack) {}
        };

        Assert.assertFalse("null block NxSensorPairs", layoutTrackEditor.hasNxSensorPairs(null));
    }

    @Test
    public void testHasNxSensorPairsDisconnectedBlock() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutTrackEditor layoutTrackEditor = new LayoutTrackEditor(null) { // core of abstract class
            public void editLayoutTrack(@Nonnull LayoutTrack layoutTrack) {}
        };

        LayoutBlock b = new LayoutBlock("test", "test");
        Assert.assertFalse("disconnected block NxSensorPairs", layoutTrackEditor.hasNxSensorPairs(b));
    }

    @Test
    public void testShowSensorMessage() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutTrackEditor layoutTrackEditor = new LayoutTrackEditor(null) { // core of abstract class
            public void editLayoutTrack(@Nonnull LayoutTrack layoutTrack) {}
        };

        layoutTrackEditor.sensorList.add("Test");
        Assert.assertFalse(layoutTrackEditor.sensorList.isEmpty());
        
        layoutTrackEditor.showSensorMessage();
    }
    
    @BeforeEach
    @OverridingMethodsMustInvokeSuper  // invoke first
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    @OverridingMethodsMustInvokeSuper  // invoke last
    public void tearDown()  {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    protected Turnout turnout0 = null;
    protected Turnout turnout1 = null;
    
    /*
     * This is used to find a component by matching against its tooltip
     */
    protected static class ToolTipComponentChooser implements ComponentChooser {

        private String buttonTooltip;
        private StringComparator comparator = Operator.getDefaultStringComparator();

        public ToolTipComponentChooser(String buttonTooltip) {
            this.buttonTooltip = buttonTooltip;
        }

        public boolean checkComponent(Component comp) {
            return comparator.equals(((JComponent) comp).getToolTipText(), buttonTooltip);
        }

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
