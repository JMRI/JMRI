package jmri.jmrix.ecos.utilities;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class GetEcosObjectNumberTest {

    @Test
    public void testCTor() {
        GetEcosObjectNumber t = new GetEcosObjectNumber();
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

    // private final static Logger log = LoggerFactory.getLogger(GetEcosObjectNumberTest.class);

}
