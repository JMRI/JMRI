package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrackEditCommentsFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location l = new Location("Location Test Attridutes id", "Location Test Name");
        Track tr = new Track("Test id", "Test Name", "Test Type", l);
        TrackEditCommentsFrame t = new TrackEditCommentsFrame(tr);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackEditCommentsFrameTest.class);
}
