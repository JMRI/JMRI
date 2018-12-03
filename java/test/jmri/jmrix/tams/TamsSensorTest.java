package jmri.jmrix.tams;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TamsSensorTest extends jmri.implementation.AbstractSensorTestBase {

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkOnMsgSent() {}

    @Override
    public void checkOffMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        t = new TamsSensor("TS1");
    }

    @Override
    @After
    public void tearDown() {
	t.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TamsSensorTest.class);

}
