package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutDoubleXOverEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LayoutDoubleXOverEditorTest extends LayoutXOverEditorTest {

    @Test
    @Override
    public void testCtor() {
        LayoutDoubleXOverEditor t = new LayoutDoubleXOverEditor(layoutEditor);
        Assertions.assertNotNull(t);

    }
 
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutDoubleXOverEditorTest.class);
}
