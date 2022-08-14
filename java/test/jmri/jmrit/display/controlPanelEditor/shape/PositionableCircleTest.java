package jmri.jmrit.display.controlPanelEditor.shape;

import jmri.jmrit.display.EditorScaffold;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class PositionableCircleTest extends PositionableShapeTest {

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        editor = new EditorScaffold();
        p = new PositionableCircle(editor);

    }

    // private final static Logger log = LoggerFactory.getLogger(PositionableCircleTest.class);

}
