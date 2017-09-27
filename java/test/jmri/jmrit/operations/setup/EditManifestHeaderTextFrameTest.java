package jmri.jmrit.operations.setup;

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
public class EditManifestHeaderTextFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditManifestHeaderTextFrame t = new EditManifestHeaderTextFrame();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testEditManifestHeaderTextFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditManifestHeaderTextFrame f = new EditManifestHeaderTextFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents();

        // TODO do more testing

        // done
        JUnitUtil.dispose(f);
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

    // private final static Logger log = LoggerFactory.getLogger(EditManifestHeaderTextFrameTest.class);

}
