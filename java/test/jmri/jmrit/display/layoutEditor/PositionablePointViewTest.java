package jmri.jmrit.display.layoutEditor;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of PositionablePointView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class PositionablePointViewTest extends LayoutTrackViewTest {

    @Test
    public void testCtor() {
        new PositionablePointView(null);
    }

}
