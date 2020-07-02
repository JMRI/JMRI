package jmri.jmrix.acela;

import jmri.Manager.NameValidity;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AcelaAddressTest {

    @Test
    public void testValidSystemNameFormat() {
        Assert.assertTrue("valid format - AL2", NameValidity.VALID == AcelaAddress.validSystemNameFormat("AL2", 'L', "A"));
        Assert.assertTrue("valid format - AT11", NameValidity.VALID == AcelaAddress.validSystemNameFormat("AT11", 'T', "A"));
        Assert.assertTrue("valid format - AS2", NameValidity.VALID == AcelaAddress.validSystemNameFormat("AS2", 'S', "A"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AcelaAddressTest.class);

}
