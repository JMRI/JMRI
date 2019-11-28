package jmri.jmrit.operations.trains.tools;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainsTableSetColorActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        TrainsTableSetColorAction t = new TrainsTableSetColorAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainsTableSetColorActionTest.class);

}
