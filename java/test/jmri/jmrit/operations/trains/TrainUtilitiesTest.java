package jmri.jmrit.operations.trains;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainUtilitiesTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        TrainUtilities t = new TrainUtilities();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainUtilitiesTest.class);

}
