package jmri.util;

import java.awt.GraphicsEnvironment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AbstractFrameActionTest {

    @Test
    public void testCtor() {
        AbstractFrameAction t = new AbstractFrameAction("TestAction","jmri.util.JmriJFrame"){
        };
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractFrameAction t = new AbstractFrameAction("TestAction","jmri.util.JmriJFrame"){
        };
        t.actionPerformed(new java.awt.event.ActionEvent(this,1,"test action event")); 
        // this test creates a JmriJFrame with no title.  find that
        javax.swing.JFrame f = JFrameOperator.waitJFrame("", true, true);
        Assert.assertNotNull("found output frame", f);
        // then close the frame.
        JFrameOperator fo = new JFrameOperator(f);
        fo.close();

    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        
        JUnitUtil.tearDown();
    }

}
