package jmri.jmrix.powerline.cp290.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SpecificSensorManagerXmlTest {

    @Test
    public void testCTor() {
        SpecificSensorManagerXml t = new SpecificSensorManagerXml();
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

    // private final static Logger log = LoggerFactory.getLogger(SpecificSensorManagerXmlTest.class);

}
