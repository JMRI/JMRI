package jmri.jmrix.ztc.ztc611.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ZTC611XNetTurnoutManagerXmlTest {

    @Test
    public void testCTor() {
        ZTC611XNetTurnoutManagerXml t = new ZTC611XNetTurnoutManagerXml();
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ZTC611XNetTurnoutManagerXmlTest.class);

}
