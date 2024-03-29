package jmri.jmrit.display.layoutEditor;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutXOverView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LayoutXOverViewTest extends LayoutTurnoutViewTest {

    // LayoutXOverView is abstract, so there's
    // not much we can do here right now.

}
