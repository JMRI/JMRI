package jmri.jmrit.display;

import org.junit.jupiter.api.*;
import org.junit.Assert;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of AnalogClock2Display
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class AnalogClock2DisplayTest extends PositionableJComponentTest {

    private AnalogClock2Display a = null;

    @Test
    public void testUrlCtor(){

        AnalogClock2Display a1 = new AnalogClock2Display(editor,"foo");
        Assert.assertNotNull("AnalogClock2Display url Constructor",a1);
        a1.dispose();
    }

    @Test
    public void testGetFaceWidth(){

        Assert.assertEquals("Face Width",166,a.getFaceWidth());
    }

    @Test
    public void testGetFaceWeight(){

        Assert.assertEquals("Face Height",166,a.getFaceHeight());
    }

    @Test
    public void testGetAndSetURL(){

        Assert.assertNull("URL before set",a.getURL());
        a.setULRL("bar");
        Assert.assertEquals("URL after set","bar",a.getURL());
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        editor = new EditorScaffold();
        p = a = new AnalogClock2Display(editor);
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (a != null) {
            a.dispose();
        }
        super.tearDown();
    }

}
