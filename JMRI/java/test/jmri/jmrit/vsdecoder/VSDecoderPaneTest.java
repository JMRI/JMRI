package jmri.jmrit.vsdecoder;

import java.awt.GraphicsEnvironment;
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
public class VSDecoderPaneTest extends jmri.util.swing.JmriPanelTest {

    @Test
    @Override
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VSDecoderFrame vf = new VSDecoderFrame();
        VSDecoderPane t = new VSDecoderPane(vf);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(vf);
    }

    @Test
    @Override
    public void testGetHelpTarget() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VSDecoderFrame vf = new VSDecoderFrame();
        VSDecoderPane t = new VSDecoderPane(vf);
        Assert.assertEquals("help target","package.jmri.jmrit.vsdecoder.VSDecoderPane",t.getHelpTarget());
        JUnitUtil.dispose(vf);
    }

    @Test
    @Override
    public void testGetTitle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VSDecoderFrame vf = new VSDecoderFrame();
        VSDecoderPane t = new VSDecoderPane(vf);
        Assert.assertEquals("title",Bundle.getMessage("WindowTitle"),t.getTitle());
        JUnitUtil.dispose(vf);
    }

    @Test
    @Override
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
    @Override
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(VSDecoderPaneTest.class);

}
