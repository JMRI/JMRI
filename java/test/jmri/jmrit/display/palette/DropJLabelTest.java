package jmri.jmrit.display.palette;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;

import javax.swing.Icon;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DropJLabelTest.class);

}
