package jmri.jmrit.operations.setup;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EditManifestTextFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditManifestTextFrame t = new EditManifestTextFrame();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testEditManifestTextFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditManifestTextFrame f = new EditManifestTextFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents();

        // TODO do more testing

        // done
        JUnitUtil.dispose(f);
    }

    // private final static Logger log = LoggerFactory.getLogger(EditManifestTextFrameTest.class);

}
