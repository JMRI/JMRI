package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SprogReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new SprogReply();
    }

    @After
    public void tearDown() {
	m = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SprogReplyTest.class);

}
