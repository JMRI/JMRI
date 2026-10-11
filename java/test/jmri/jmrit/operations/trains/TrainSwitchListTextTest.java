package jmri.jmrit.operations.trains;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainSwitchListTextTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        TrainSwitchListText t = new TrainSwitchListText();
        Assert.assertNotNull("exists",t);
    }

    // private static final Logger log = LoggerFactory.getLogger(TrainSwitchListTextTest.class);

}
