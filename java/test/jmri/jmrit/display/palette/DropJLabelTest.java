package jmri.jmrit.display.palette;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.Icon;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.GraphicsEnvironment;

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
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DropJLabelTest.class.getName());

}
