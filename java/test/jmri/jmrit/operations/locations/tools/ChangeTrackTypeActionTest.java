package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.locations.TrackEditFrame;
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
public class ChangeTrackTypeActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrackEditFrame tf = new TrackEditFrame();
        ChangeTrackTypeAction t = new ChangeTrackTypeAction(tf);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(tf);
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

    // private final static Logger log = LoggerFactory.getLogger(ChangeTrackTypeActionTest.class);

}
