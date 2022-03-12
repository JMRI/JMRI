package jmri.util.swing;

import java.awt.Color;
import java.awt.image.BufferedImage;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DrawSquaresTest.class);

}
