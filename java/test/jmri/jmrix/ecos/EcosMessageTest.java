package jmri.jmrix.ecos;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EcosMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new EcosMessage();
    }

    @AfterEach
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EcosMessageTest.class);

}
