package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of FunctionPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class FunctionPanelTest {
        
    FunctionPanel frame; // not a panel despite class name

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", frame);
    }

    @Test
    public void testGetFunctionButtons(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        FunctionButton fba[] = frame.getFunctionButtons();
	    Assert.assertNotNull("Function Button Array exists",fba);
	    Assert.assertEquals("Function Button Array has right length",29,fba.length);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new FunctionPanel();
        }
    }

    @After
    public void tearDown() {
        if(frame!=null){
          frame.dispose();
        }
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }
}
