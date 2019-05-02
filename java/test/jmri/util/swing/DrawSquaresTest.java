package jmri.util.swing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Egbert Broerse Copyright (C) 2017
 */
public class DrawSquaresTest {

    @Test
    public void testCall() {
        BufferedImage bi = DrawSquares.getImage(100, 100, 25, Color.white, Color.black);
        Assert.assertNotNull("exists", bi);
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

    // private final static Logger log = LoggerFactory.getLogger(DrawSquaresTest.class);

}
