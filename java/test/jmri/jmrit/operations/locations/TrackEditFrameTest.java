package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrackEditFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrackEditFrame t = new TrackEditFrame("test");
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackEditFrameTest.class);

}
