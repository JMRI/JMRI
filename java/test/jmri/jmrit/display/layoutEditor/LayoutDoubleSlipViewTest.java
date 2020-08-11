package jmri.jmrit.display.layoutEditor;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutDoubleSlipView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutDoubleSlipViewTest extends LayoutSlipViewTest {

    @Test
    public void testCtor() {
        new LayoutDoubleSlipView(null);
    }
}
