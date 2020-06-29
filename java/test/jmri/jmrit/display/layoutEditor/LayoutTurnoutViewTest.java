package jmri.jmrit.display.layoutEditor;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutTurnoutView
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LayoutTurnoutViewTest extends LayoutTrackViewTest {

    @Test
    public void testCtor() {
        new LayoutTurnoutView(null);
    }

}
