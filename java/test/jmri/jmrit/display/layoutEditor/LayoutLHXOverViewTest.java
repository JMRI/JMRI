package jmri.jmrit.display.layoutEditor;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutLHXOverView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class  LayoutLHXOverViewTest extends  LayoutXOverViewTest {

    @Test
    public void testCtor() {
        new LayoutLHXOverView(null);
    }
}
