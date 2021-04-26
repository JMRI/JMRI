package jmri.util;

import java.awt.GraphicsEnvironment;

import javax.help.JHelpContentViewer;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExternalLinkContentViewerUITest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ExternalLinkContentViewerUI t = new ExternalLinkContentViewerUI(new JHelpContentViewer());
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

    // private final static Logger log = LoggerFactory.getLogger(ExternalLinkContentViewerUITest.class);

}
