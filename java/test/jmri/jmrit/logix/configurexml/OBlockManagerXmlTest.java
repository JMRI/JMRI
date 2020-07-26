package jmri.jmrit.logix.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OBlockManagerXmlTest {

    @Test
    @Disabled("causes missing data for other tests?")
    public void testCTor() {
        OBlockManagerXml t = new OBlockManagerXml();
        assertThat(t).withFailMessage("exists").isNotNull();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(OBlockManagerXmlTest.class);

}
