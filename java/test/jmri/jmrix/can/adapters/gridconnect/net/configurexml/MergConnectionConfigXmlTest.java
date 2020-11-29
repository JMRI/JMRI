package jmri.jmrix.can.adapters.gridconnect.net.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MergConnectionConfigXmlTest {

    @Test
    public void testCTor() {
        MergConnectionConfigXml t = new MergConnectionConfigXml();
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

    // private final static Logger log = LoggerFactory.getLogger(MergConnectionConfigXmlTest.class);

}
