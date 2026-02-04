package jmri.jmrix.ecos.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class GetEcosObjectNumberTest {

    // no Ctor test, class only supplies static methods

    @Test
    public void testGetEcosObjectNumber() {
        assertEquals( 123, GetEcosObjectNumber.getEcosObjectNumber("123", null, null));
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
