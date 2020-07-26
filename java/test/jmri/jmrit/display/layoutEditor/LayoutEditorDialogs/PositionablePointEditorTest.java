package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.GraphicsEnvironment;

import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of PositionablePointEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class PositionablePointEditorTest extends LayoutTrackEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new PositionablePointEditor(null);
    }
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PositionablePointEditorTest.class);
}
