package jmri.jmrix.bachrus;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * SpeedoReplyTest.java
 *
 * Description:	tests for the jmri.jmrix.bachrus.SpeedoReply class
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SpeedoReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new SpeedoReply();
    }
   
    @After
    public void tearDown() {
	m = null;
        JUnitUtil.tearDown();
    }

}
