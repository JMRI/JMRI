package jmri.jmrit.display.controlPanelEditor.shape;

import jmri.jmrit.display.EditorScaffold;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class PositionableRoundRectTest extends PositionableRectangleTest {

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        editor = new EditorScaffold();
        p = new PositionableRoundRect(editor);

    }

    // private final static Logger log = LoggerFactory.getLogger(PositionableRoundRectTest.class);

}
