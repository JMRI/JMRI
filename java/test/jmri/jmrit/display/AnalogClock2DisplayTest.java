package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of AnalogClock2Display
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class AnalogClock2DisplayTest extends PositionableJComponentTest {

    private AnalogClock2Display a = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("AnalogClock2Display Constructor",p);
    }

    @Test
    public void testUrlCtor(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AnalogClock2Display a1 = new AnalogClock2Display(editor,"foo");
        Assert.assertNotNull("AnalogClock2Display url Constructor",a1);
        a1.dispose();
    }

    @Test
    public void testGetFaceWidth(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Face Width",166,a.getFaceWidth());
    }

    @Test
    public void testGetFaceWeight(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Face Height",166,a.getFaceHeight());
    }

    @Test
    public void testGetAndSetURL(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNull("URL before set",a.getURL());
        a.setULRL("bar");
        Assert.assertEquals("URL after set","bar",a.getURL());
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           editor = new EditorScaffold();
           p = a = new AnalogClock2Display(editor);
        }
    }

    @After
    @Override
    public void tearDown() {
        if (a != null) {
            a.dispose();
        }
        super.tearDown();
    }


}
