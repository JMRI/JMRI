package jmri.jmrix.lenz.hornbyelite.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EliteXNetTurnoutManagerXmlTest {

    @Test
    public void testCTor() {
        EliteXNetTurnoutManagerXml t = new EliteXNetTurnoutManagerXml();
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

    // private final static Logger log = LoggerFactory.getLogger(EliteXNetTurnoutManagerXmlTest.class);

}
