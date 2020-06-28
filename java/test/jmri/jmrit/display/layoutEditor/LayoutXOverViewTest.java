package jmri.jmrit.display.layoutEditor;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutXOverView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutXOverViewTest extends LayoutTurnoutViewTest {

    @Test
    public void testCtor() {
        new LayoutXOverView(null);
    }

}
