package jmri.jmrit.display.layoutEditor;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutSlipView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutSlipViewTest extends LayoutTurnoutViewTest {

    @Test
    public void testCtor() {
        new LayoutSlipView(null);
    }

}
