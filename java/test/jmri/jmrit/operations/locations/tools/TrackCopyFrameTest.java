package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.LocationEditFrame;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrackCopyFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocationEditFrame f = new LocationEditFrame(null);
        TrackCopyFrame t = new TrackCopyFrame(f);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(f);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackCopyFrameTest.class);

}
