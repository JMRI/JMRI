package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MarklinSensorTest extends jmri.implementation.AbstractSensorTestBase {

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkOnMsgSent() {}

    @Override
    public void checkOffMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        t = new MarklinSensor("MCS1:2");
    }

    @Override
    @After
    public void tearDown() {
        t.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MarklinSensorTest.class);

}
