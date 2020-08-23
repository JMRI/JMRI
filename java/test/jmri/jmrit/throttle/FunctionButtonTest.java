package jmri.jmrit.throttle;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of FunctionButton
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class FunctionButtonTest {

    @Test
    public void testCtor() {
        FunctionButton panel = new FunctionButton();
        Assert.assertNotNull("exists", panel );
    }

    @Test
    public void testIsOn() {
        FunctionButton panel = new FunctionButton();
        Assert.assertFalse("function not on", panel.getState() );
    }

    @Test
    public void testGetIconPath() {
        FunctionButton panel = new FunctionButton();
        Assert.assertEquals("no Icon", "", panel.getIconPath() );
    }

    @Test
    public void testIsImageOK() {
        FunctionButton panel = new FunctionButton();
        Assert.assertFalse("no image", panel.isImageOK() );
    }

    @Test
    public void testGetSelectedIconPath() {
        FunctionButton panel = new FunctionButton();
        Assert.assertEquals("no Icon", "", panel.getSelectedIconPath() );
    }
  
    @Test
    public void testIsSelectedImageOK() {
        FunctionButton panel = new FunctionButton();
        Assert.assertFalse("no image", panel.isSelectedImageOK() );
    }

    @BeforeEach
    public void setUp(){
        jmri.util.JUnitUtil.setUp();

    }
    
    @AfterEach
    public void tearDown(){
        jmri.util.JUnitUtil.tearDown();

    }
}
