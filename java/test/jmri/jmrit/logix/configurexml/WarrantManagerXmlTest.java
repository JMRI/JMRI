package jmri.jmrit.logix.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class WarrantManagerXmlTest {

    @Test
    @Disabled("causes missing data for other tests?")
    public void testCTor() {
        WarrantManagerXml t = new WarrantManagerXml();
        assertThat(t).withFailMessage("exists").isNotNull();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // should be converted to check of scheduled ShutDownActions
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(WarrantManagerXmlTest.class);

}
