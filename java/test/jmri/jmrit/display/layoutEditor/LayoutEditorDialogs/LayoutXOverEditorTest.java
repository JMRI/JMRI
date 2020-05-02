package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import javax.swing.*;
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
 * Test simple functioning of LayoutXOverEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutXOverEditorTest extends LayoutTurnoutEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new LayoutXOverEditor(null);
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutXOverEditorTest.class);
}
