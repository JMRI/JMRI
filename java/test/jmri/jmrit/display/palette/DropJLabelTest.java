package jmri.jmrit.display.palette;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import javax.swing.Icon;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DropJLabelTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Icon i = new Icon(){
           @Override 
           public int getIconHeight(){
              return 0;
           }
           @Override 
           public int getIconWidth(){
              return 0;
           }
           @Override
           public void paintIcon(Component c, Graphics g,int x, int y){
           }
        };
        DropJLabel t = new DropJLabel(i);
        Assert.assertNotNull("exists",t);
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

    // private final static Logger log = LoggerFactory.getLogger(DropJLabelTest.class);

}
