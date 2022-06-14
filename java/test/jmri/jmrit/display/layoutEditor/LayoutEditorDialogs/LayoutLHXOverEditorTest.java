package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;


import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutLHXOverEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LayoutLHXOverEditorTest extends LayoutXOverEditorTest {

    @Test
    @Override
    public void testCtor() {
        LayoutLHXOverEditor t = new LayoutLHXOverEditor(layoutEditor);
        Assertions.assertNotNull(t);

    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutLHXOverEditorTest.class);
}
