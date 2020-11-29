package jmri.jmrix.direct;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MessageTest extends jmri.jmrix.AbstractMessageTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new Message(5);
    }

    @AfterEach
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MessageTest.class);

}
