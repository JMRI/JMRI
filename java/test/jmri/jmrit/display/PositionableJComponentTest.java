package jmri.jmrit.display;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.junit.annotations.*;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfHeadless
public class PositionableJComponentTest extends PositionableTestBase {

    @Test
    public void testCtor() {
        assertNotNull( p, "exists");
    }

    @Override
    @Test
    public void testGetAndSetViewCoordinates() {

        assertFalse( p.getViewCoordinates(), "Defalt View Coordinates");
        p.setViewCoordinates(true);
        assertTrue( p.getViewCoordinates(), "View Coordinates after set true");
        p.setViewCoordinates(false);
        assertFalse( p.getViewCoordinates(), "View Coordinates after set false");
    }

    @Test
    @Override
    @Disabled("PositionableJComponent does not support rotate")
    @ToDo("implement rotate in PositionableJComponent, then remove overriden test so parent class test can execute")
    public void testGetAndSetRotationDegrees() {
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        editor = new EditorScaffold();
        p = new PositionableJComponent(editor);
        ((PositionableJComponent) p).setName("PositionableJComponent");

    }

}
