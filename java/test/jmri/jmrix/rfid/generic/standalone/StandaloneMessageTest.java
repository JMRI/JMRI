package jmri.jmrix.rfid.generic.standalone;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class StandaloneMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new StandaloneMessage(5);
    }

    @After
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(StandaloneMessageTest.class);
}
