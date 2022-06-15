package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutRHXOverEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LayoutRHXOverEditorTest extends LayoutXOverEditorTest {

    @Test
    @Override
    public void testCtor() {
        LayoutRHXOverEditor t = new LayoutRHXOverEditor(layoutEditor);
        Assertions.assertNotNull(t);
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutRHOXOverEditorTest.class);
}
