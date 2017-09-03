package jmri.implementation;

import jmri.SignalHead;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class VirtualSignalHeadTest extends AbstractSignalHeadTestBase {

    @Test
    public void testCTor() {
        VirtualSignalHead t = new VirtualSignalHead("Virtual Signal Head Test");
        Assert.assertNotNull("exists",t);
    }

    @Override
    public SignalHead getHeadToTest() {
        return new VirtualSignalHead("Virtual Signal Head Test");
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

    //private final static Logger log = LoggerFactory.getLogger(VirtualSignalHeadTest.class);

}
