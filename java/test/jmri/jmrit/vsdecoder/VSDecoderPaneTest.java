package jmri.jmrit.vsdecoder;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class VSDecoderPaneTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VSDecoderFrame vf = new VSDecoderFrame();
        VSDecoderPane t = new VSDecoderPane(vf);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(vf);
    }

    @Test
    public void testGetHelpTarget() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VSDecoderFrame vf = new VSDecoderFrame();
        VSDecoderPane t = new VSDecoderPane(vf);
        Assert.assertEquals("help target","package.jmri.jmrit.log.Log4JTreePane",t.getHelpTarget());
        JUnitUtil.dispose(vf);
    }

    @Test
    public void testGetTitle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VSDecoderFrame vf = new VSDecoderFrame();
        VSDecoderPane t = new VSDecoderPane(vf);
        Assert.assertEquals("title",Bundle.getMessage("MenuItemLogTreeAction"),t.getTitle());
        JUnitUtil.dispose(vf);
    }

    @Test
    public void testInitComponents() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VSDecoderFrame vf = new VSDecoderFrame();
        VSDecoderPane t = new VSDecoderPane(vf);
        // we are just making sure that initComponents doesn't cause an exception.
        t.initComponents();
        JUnitUtil.dispose(vf);
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

    // private final static Logger log = LoggerFactory.getLogger(VSDecoderPaneTest.class);

}
