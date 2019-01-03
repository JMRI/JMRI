package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class YardmasterByTrackActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        YardmasterByTrackAction t = new YardmasterByTrackAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(YardmasterByTrackActionTest.class);

}
