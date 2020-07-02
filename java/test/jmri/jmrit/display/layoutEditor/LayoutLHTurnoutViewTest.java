package jmri.jmrit.display.layoutEditor;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutLHTurnoutView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutLHTurnoutViewTest extends LayoutTurnoutViewTest {

    @Test
    public void testCtor() {
        new LayoutLHTurnoutView(null);
    }
}
