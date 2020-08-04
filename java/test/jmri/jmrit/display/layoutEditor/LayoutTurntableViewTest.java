package jmri.jmrit.display.layoutEditor;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutTurntableView
 *
 * @author Bob Jacobsen Copyright (C) 2016
 */
public class LayoutTurntableViewTest extends LayoutTrackViewTest {

    @Test
    public void testCtor() {
        new LayoutTurntableView(null);
    }
}
