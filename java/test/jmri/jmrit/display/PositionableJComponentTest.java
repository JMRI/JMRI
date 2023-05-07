package jmri.jmrit.display;

import jmri.util.junit.annotations.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class PositionableJComponentTest extends PositionableTestBase {

    @Test
    public void testCtor() {

        Assert.assertNotNull("exists", p);
    }

    @Override
    @Test
    public void testGetAndSetViewCoordinates() {

        Assert.assertFalse("Defalt View Coordinates", p.getViewCoordinates());
        p.setViewCoordinates(true);
        Assert.assertTrue("View Coordinates after set true", p.getViewCoordinates());
        p.setViewCoordinates(false);
        Assert.assertFalse("View Coordinates after set false", p.getViewCoordinates());
    }

    @Test
    @Override
    @Disabled("PositionableJComponent does not support rotate")
    @ToDo("implement rotate in PositionableJComponent, then remove overriden test so parent class test can execute")
    public void testGetAndSetRotationDegrees() {
        Assert.fail("Should support rotation but doesn't");
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
