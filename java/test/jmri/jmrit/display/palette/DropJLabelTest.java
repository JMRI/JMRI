package jmri.jmrit.display.palette;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DropJLabelTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
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
        Assertions.assertNotNull(t,"exists");
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
