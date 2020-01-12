package jmri.jmrix.can;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Common tests for the jmri.jmrix.can.CanMessage and CanReply classes
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public class CanMRCommonTestBase extends jmri.jmrix.AbstractMessageTestBase {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
	m = null;
        JUnitUtil.tearDown();
    }

}
