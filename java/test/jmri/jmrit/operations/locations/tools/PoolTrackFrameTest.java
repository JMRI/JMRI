package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.YardEditFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PoolTrackFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        YardEditFrame tf = new YardEditFrame();
        PoolTrackFrame t = new PoolTrackFrame(tf);
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(PoolTrackFrameTest.class);

}
