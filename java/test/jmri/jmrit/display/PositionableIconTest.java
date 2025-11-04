package jmri.jmrit.display;

import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of PositionableIcon.
 * Extending classes should use DisabledIfHeadless annotation as the setUp
 * method of this class creates an EditorScaffold.
 * @author Paul Bender Copyright (C) 2016
 */
public class PositionableIconTest extends PositionableTestBase {

    @Test
    @DisabledIfHeadless
    public void testCtor() {
        Assertions.assertNotNull( p, "PositionableIcon Constructor");
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        editor = new EditorScaffold();
        p = new PositionableIcon(editor);
    }

}
