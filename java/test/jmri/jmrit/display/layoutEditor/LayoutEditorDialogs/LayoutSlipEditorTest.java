package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutSlipEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LayoutSlipEditorTest extends LayoutTurnoutEditorTest {

    @Test
    @Override
    public void testCtor() {

        LayoutSlipEditor t = new LayoutSlipEditor(layoutEditor);
        Assertions.assertNotNull(t);
    }
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSlipEditorTest.class);
}
