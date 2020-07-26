package jmri.jmrit.display.layoutEditor;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LevelXingView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LevelXingViewTest extends LayoutTrackViewTest {

    @Test
    public void testCtor() {
        new LevelXingView(null);
    }

}
