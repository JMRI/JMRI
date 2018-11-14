package jmri.util;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JmriJFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriJFrame t1 = new JmriJFrame("JmriJFrame ConstructorTest-1");
        Assert.assertNotNull("exists",t1);
        t1.setSize(new Dimension(113,181));
        t1.setSaveSize(true);
        JUnitUtil.dispose(t1);
        
        JmriJFrame t2 = new JmriJFrame("JmriJFrame ConstructorTest-2");
        Assert.assertNotNull("exists",t2);
        t2.setSaveSize(false);
        JUnitUtil.dispose(t2);
        
        JmriJFrame t3 = new JmriJFrame("JmriJFrame ConstructorTest-3");
        Assert.assertNotNull("exists",t3);
        t3.setSize(new Dimension(191,127));
        t3.setSaveSize(true);
        JUnitUtil.dispose(t3);

        // recreate and check size
        JmriJFrame t4 = new JmriJFrame("JmriJFrame ConstructorTest-1");
        Assert.assertNotNull("exists",t2);
        Dimension d4 = t4.getSize();
        Assert.assertEquals("Test Height",181, d4.getHeight(),0);
        Assert.assertEquals("Test Width",113, d4.getWidth(),0);
        JUnitUtil.dispose(t3);

        JmriJFrame t5 = new JmriJFrame("JmriJFrame ConstructorTest-2");
        Assert.assertNotNull("exists",t5);
        Dimension d5 = t5.getSize();
        Assert.assertEquals("Test Height",0, d5.getHeight(),0);
        Assert.assertEquals("Test Width",0, d5.getWidth(),0);
        JUnitUtil.dispose(t5);

        JmriJFrame t7 = new JmriJFrame("JmriJFrame ConstructorTest-3");
        Assert.assertNotNull("exists",t7);
        Dimension d7 = t7.getSize();
        Assert.assertEquals("Test Height",127, d7.getHeight(),0);
        Assert.assertEquals("Test Width",191, d7.getWidth(),0);
        JUnitUtil.dispose(t7);
        
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JmriJFrameTest.class);

}
