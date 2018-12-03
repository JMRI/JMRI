package jmri.jmrix.dcc4pc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Dcc4PcMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = new Dcc4PcMessage(5);
    }

    @After
    public void tearDown() {
	m = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Dcc4PcMessageTest.class);

}
