package jmri.util.swing;

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
public class ImagePanelTest {

    @Test
    public void testCall() {
        ImagePanel ip = new ImagePanel();
        Assert.assertNotNull("exists", ip);
    }

    @Test
    public void setImage() {
        ImagePanel ip = new ImagePanel();
        BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_ARGB);
        ip.setImage(img);
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

    // private final static Logger log = LoggerFactory.getLogger(ImagePanelTest.class);

}
