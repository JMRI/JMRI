package jmri.jmrit.operations.trains;

import jmri.jmrit.operations.OperationsTestCase;

import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainsTableActionTest extends OperationsTestCase {

    @Rule
    public org.junit.rules.Timeout globalTimeout = org.junit.rules.Timeout.seconds(10);

    @Rule
    public jmri.util.junit.rules.RetryRule retryRule = new jmri.util.junit.rules.RetryRule(3); // first, plus three retries

    @Test
    public void testCTor() {
        TrainsTableAction t = new TrainsTableAction();
        Assert.assertNotNull("exists", t);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainsTableActionTest.class);

}
