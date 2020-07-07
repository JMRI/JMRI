package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SprogReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new SprogReply();
    }

    @AfterEach
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SprogReplyTest.class);

}
