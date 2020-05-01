package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import javax.swing.*;
import javax.annotation.*;
import jmri.*;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;
import jmri.util.junit.rules.RetryRule;
import jmri.util.swing.JemmyUtil;
import org.junit.*;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * Test simple functioning of LayoutTrackEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutTrackEditorTest {

    @Test
    public void testCtor() {
        new LayoutTrackEditor(null);
    }

    @Test
    public void testHasNxSensorPairsNull() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutTrackEditor layoutTrackEditor = new LayoutTrackEditor(null);

        Assert.assertFalse("null block NxSensorPairs", layoutTrackEditor.hasNxSensorPairs(null));
    }

    @Test
    public void testHasNxSensorPairsDisconnectedBlock() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutTrackEditor layoutTrackEditor = new LayoutTrackEditor(null);

        LayoutBlock b = new LayoutBlock("test", "test");
        Assert.assertFalse("disconnected block NxSensorPairs", layoutTrackEditor.hasNxSensorPairs(b));
    }

    @Test
    public void testShowSensorMessage() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutTrackEditor layoutTrackEditor = new LayoutTrackEditor(null);

        layoutTrackEditor.sensorList.add("Test");
        Assert.assertFalse(layoutTrackEditor.sensorList.isEmpty());
        
        layoutTrackEditor.showSensorMessage();
    }
    
    @Before
    @OverridingMethodsMustInvokeSuper  // invoke first
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    @OverridingMethodsMustInvokeSuper  // invoke last
    public void tearDown()  {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTrackEditorTest.class);
}
